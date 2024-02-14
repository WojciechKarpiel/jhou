package pl.wojciechkarpiel.jhou.unifier;

import pl.wojciechkarpiel.jhou.substitution.Substitution;
import pl.wojciechkarpiel.jhou.unifier.tree.Tree;
import pl.wojciechkarpiel.jhou.unifier.tree.UsedUpNodes;
import pl.wojciechkarpiel.jhou.unifier.tree.WeBackNode;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

public class SolutionIterator implements Iterator<Substitution> {
    public static final int UNLIMITED_ITERATIONS = -1;

    private final UsedUpNodes usedUpNodes;
    private final Tree tree;
    private final int maxSearchDepth;
    private int expansionsSoFar;

    public SolutionIterator(Tree tree) {
        this(tree, UNLIMITED_ITERATIONS);
    }

    public SolutionIterator(Tree tree, int maxSearchDepth) {
        this.tree = tree;
        if (maxSearchDepth < 0) {
            this.maxSearchDepth = Integer.MAX_VALUE;
        } else {
            this.maxSearchDepth = maxSearchDepth;
        }
        this.usedUpNodes = UsedUpNodes.empty();
        this.expansionsSoFar = 0;
    }

    private boolean itsOver() {
        return tree.itsOver(usedUpNodes);
    }

    private Optional<WeBackNode> weBack() {
        return tree.weBack(usedUpNodes);
    }

    private boolean couldExpand() {
        if (expansionsSoFar >= maxSearchDepth) {
            System.out.println("Max search depth reached, aborting");
            return false;
        }
        return !(itsOver() || weBack().isPresent());
    }

    private void doExpand() {
        if (expansionsSoFar >= maxSearchDepth) {
            throw new MaxSearchDepthExceededException();
        }
        expansionsSoFar++;
        System.out.println("Will expand the search tree for " + expansionsSoFar + "sh time");
        tree.expandOnce();
    }

    private Optional<WeBackNode> tryNext() {
        while (true) {
            if (itsOver()) return Optional.empty();
            if (weBack().isPresent()) return weBack();
            if (!couldExpand()) return Optional.empty();
            doExpand();
        }
    }

    @Override
    public boolean hasNext() {
        return tryNext().isPresent();
    }

    @Override
    public Substitution next() {
        Optional<WeBackNode> weBack = tryNext();
        if (weBack.isPresent()) {
            WeBackNode weSoBack = weBack.get();
            usedUpNodes.addUsedUpNode(weSoBack);
            return weSoBack.fullSolution();
        } else {
            throw new NoSuchElementException();
        }
    }
}
