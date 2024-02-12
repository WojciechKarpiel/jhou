package pl.wojciechkarpiel.unifier.tree;

import pl.wojciechkarpiel.unifier.DisagreementSet;
import pl.wojciechkarpiel.unifier.simplifier.Simplifier;
import pl.wojciechkarpiel.unifier.simplifier.result.SimplificationOps;
import pl.wojciechkarpiel.unifier.simplifier.result.SimplificationResult;

import java.util.List;

public class Tree {

    private final DisagreementSet nodeStartingPoint;

    private SimplificationResult __simplificationResult;

    private List<Tree> __subtrees;

    public Tree(DisagreementSet nodeStartingPoint) {
        this.nodeStartingPoint = nodeStartingPoint;
    }

    public boolean itsOver() {
        return SimplificationOps.asSuccess(getSimplificationResult()).isPresent() ||
                SimplificationOps.asFailure(getSimplificationResult()).isPresent();
    }


    public SimplificationResult getSimplificationResult() {
        if (__simplificationResult == null) {
            __simplificationResult = Simplifier.simplify(nodeStartingPoint);
        }
        return __simplificationResult;
    }

    public List<Tree> getSubtrees() {
        if (__subtrees == null) {

        }
        return __subtrees;
    }
}
