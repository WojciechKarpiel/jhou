package pl.wojciechkarpiel.jhou.unifier.tree;

import pl.wojciechkarpiel.jhou.normalizer.Normalizer;
import pl.wojciechkarpiel.jhou.substitution.Substitution;
import pl.wojciechkarpiel.jhou.unifier.DisagreementPair;
import pl.wojciechkarpiel.jhou.unifier.DisagreementSet;
import pl.wojciechkarpiel.jhou.unifier.simplifier.Matcher;
import pl.wojciechkarpiel.jhou.unifier.simplifier.Simplifier;
import pl.wojciechkarpiel.jhou.unifier.simplifier.result.*;
import pl.wojciechkarpiel.jhou.util.ListUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WorkWorkNode implements Tree {

    private final Substitution fromParent;
    private final DisagreementSet disagreementSet;

    private List<Tree> children;
    private final Tree parent;

    public WorkWorkNode(Tree parent, Substitution fromParent, DisagreementSet disagreementSet) {
        this.parent = parent;
        this.fromParent = fromParent;
        this.disagreementSet = disagreementSet;
    }

    public Tree getParent() {
        return parent;
    }

    @Override
    public boolean itsOver(UsedUpNodes usedUpNodes) {
        if (children == null) return false;
        else return children.stream().allMatch(c -> c.itsOver(usedUpNodes));
    }

    @Override
    public Optional<WeBackNode> weBack(UsedUpNodes usedUpNodes) {
        if (children == null) return Optional.empty();
        return children.stream()
                .map(c -> c.weBack(usedUpNodes))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    @Override
    public void expandOnce() {
        if (children != null) {
            children.forEach(Tree::expandOnce);
        } else {
            SimplificationResult simplify = Simplifier.simplify(disagreementSet);

            WorkWorkNode thiz = this;
            children = simplify.visit(new SimplificationVisitor<List<Tree>>() {
                @Override
                public List<Tree> visitSuccess(SimplificationSuccess success) {
                    return ListUtil.of(new WeBackNode(thiz, success.getSolution()));
                }

                @Override
                public List<Tree> visitNode(SimplificationNode node) {

                    return createChildNodes(node.getDisagreement());
                }

                @Override
                public List<Tree> visitFailure(NonUnifiable nonUnifiable) {
                    return ListUtil.of(new NagmiNode(thiz));
                }
            });

        }
    }

    @Override
    public Substitution inheritedSubstitution() {
        return fromParent;
    }

    private List<Tree> createChildNodes(DisagreementSet disagreement) {
        List<Tree> result = new ArrayList<>();
        Stream<List<Substitution>> stream = disagreement.getDisagreements().stream().map(disagreementPair ->
                Matcher.matchS(disagreementPair.getMostRigid(), disagreementPair.getLeastRigid())
        );
        List<Substitution> flatSubs = new ArrayList<>();
        stream.forEach(flatSubs::addAll);
        for (Substitution possibleSolution : flatSubs) {
            DisagreementSet newDs = new DisagreementSet(disagreement.getDisagreements().stream().map(disagreementPair ->
                    new DisagreementPair(
                            Normalizer.betaNormalize(possibleSolution.substitute(disagreementPair.getMostRigid().backToTerm())),
                            Normalizer.betaNormalize(possibleSolution.substitute(disagreementPair.getLeastRigid().backToTerm()))
                    )).collect(Collectors.toList()));

            Tree tree = new WorkWorkNode(this, possibleSolution, newDs);
            result.add(tree);
        }

        return result;
    }
}
