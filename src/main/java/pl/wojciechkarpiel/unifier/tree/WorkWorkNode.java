package pl.wojciechkarpiel.unifier.tree;

import pl.wojciechkarpiel.normalizer.Normalizer;
import pl.wojciechkarpiel.substitution.Substitution;
import pl.wojciechkarpiel.unifier.DisagreementPair;
import pl.wojciechkarpiel.unifier.DisagreementSet;
import pl.wojciechkarpiel.unifier.simplifier.Matcher;
import pl.wojciechkarpiel.unifier.simplifier.Simplifier;
import pl.wojciechkarpiel.unifier.simplifier.result.*;
import pl.wojciechkarpiel.util.ListUtil;

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

    public DisagreementSet getDisagreementSet() {
        return disagreementSet;
    }

    public Tree getParent() {
        return parent;
    }

    @Override
    public boolean itsOver() {
        if (children == null) return false;
        else return children.stream().allMatch(Tree::itsOver);
    }

    @Override
    public Optional<WeBackNode> weBack() {
        if (children == null) return Optional.empty();
        return children.stream().map(Tree::weBack).filter(Optional::isPresent).map(Optional::get).findFirst();
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
                            Normalizer.normalize(possibleSolution.substitute(disagreementPair.getMostRigid().backToTerm())),
                            Normalizer.normalize(possibleSolution.substitute(disagreementPair.getLeastRigid().backToTerm()))
                    )).collect(Collectors.toList()));

            System.out.println("Yooo trying " + possibleSolution);
            Tree tree = Simplifier.simplify(newDs).visit(new SimplificationVisitor<Tree>() {
                @Override
                public Tree visitSuccess(SimplificationSuccess success) {
                    return new WeBackNode(WorkWorkNode.this, success.getSolution());
                }

                @Override
                public Tree visitNode(SimplificationNode node) {
                    return new WorkWorkNode(WorkWorkNode.this, possibleSolution, newDs);
                }

                @Override
                public Tree visitFailure(NonUnifiable nonUnifiable) {
                    return new NagmiNode(WorkWorkNode.this);
                }
            });
            result.add(tree);
        }

        return result;
    }
}
