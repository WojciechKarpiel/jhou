package pl.wojciechkarpiel.jhou.unifier.tree;

import pl.wojciechkarpiel.jhou.normalizer.Normalizer;
import pl.wojciechkarpiel.jhou.substitution.Substitution;
import pl.wojciechkarpiel.jhou.unifier.DisagreementPair;
import pl.wojciechkarpiel.jhou.unifier.DisagreementSet;
import pl.wojciechkarpiel.jhou.unifier.PairType;
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
    public boolean itsOver() {
        if (children == null) return false;
        else {
            boolean itsOver = children.stream().allMatch(Tree::itsOver);
            // prune the tree
            if (itsOver) {
                children = ListUtil.of(new NagmiNode(this));
            }
            return itsOver;
        }
    }

    @Override
    public Optional<WeBackNode> weBack() {
        if (children == null) return Optional.empty();
        return children.stream()
                .map(Tree::weBack)
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
        return createChildNodes(disagreement, this);
    }

    public static List<Tree> createChildNodes(DisagreementSet disagreement, Tree parent) {
        List<Tree> result = new ArrayList<>();
        Stream<List<Substitution>> stream = disagreement.getDisagreements().stream()
                .filter(dp -> dp.getType() == PairType.RIGID_FLEXIBLE)
                .map(disagreementPair ->
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

            Tree tree = new WorkWorkNode(parent, possibleSolution, newDs);
            result.add(tree);
        }

        return result;
    }
}
