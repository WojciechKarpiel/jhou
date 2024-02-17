package pl.wojciechkarpiel.jhou.unifier;

import pl.wojciechkarpiel.jhou.substitution.Substitution;
import pl.wojciechkarpiel.jhou.unifier.tree.Tree;
import pl.wojciechkarpiel.jhou.unifier.tree.WeBackNode;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

public class SolutionIterator implements Iterator<Substitution> {
    public static final int UNLIMITED_ITERATIONS = -1;
    public static final PrintStream DEFAULT_PRINT_STREAM = System.out;

    private final Tree tree;
    private final PrintStream printStream;
    private final int maxSearchDepth;
    private int expansionsSoFar;

    public SolutionIterator(Tree tree) {
        this(tree, DEFAULT_PRINT_STREAM);
    }

    public SolutionIterator(Tree tree, PrintStream printStream) {
        this(tree, UNLIMITED_ITERATIONS, printStream);
    }

    public SolutionIterator(Tree tree, int maxSearchDepth) {
        this(tree, maxSearchDepth, DEFAULT_PRINT_STREAM);
    }

    public SolutionIterator(Tree tree, int maxSearchDepth, PrintStream printStream) {
        this.tree = tree;
        this.printStream = printStream;
        if (maxSearchDepth < 0) {
            this.maxSearchDepth = Integer.MAX_VALUE;
        } else {
            this.maxSearchDepth = maxSearchDepth;
        }
        this.expansionsSoFar = 0;
    }

    private boolean itsOver() {
        return tree.itsOver();
    }

    private Optional<WeBackNode> weBack() {
        return tree.weBack();
    }

    private boolean couldExpand() {
        if (expansionsSoFar >= maxSearchDepth) {
            printStream.println("Max search depth reached, aborting");
            return false;
        }
        return !(itsOver() || weBack().isPresent());
    }

    private void doExpand() {
        if (expansionsSoFar >= maxSearchDepth) {
            throw new MaxSearchDepthExceededException();
        }
        expansionsSoFar++;
        printStream.println("Will expand the search tree for " + expansionsSoFar + "sh time");
        tree.expandOnce();
    }

    private WeBackNode tryNextCache = null;

    private void resetCache() {
        tryNextCache = null;
    }

    private Optional<WeBackNode> tryNext() {
        if (tryNextCache != null) return Optional.of(tryNextCache);
        tryNextCache = tryNextSkipCache().orElse(null);
        return Optional.ofNullable(tryNextCache);
    }

    private Optional<WeBackNode> tryNextSkipCache() {
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
            resetCache();
            WeBackNode weSoBack = weBack.get();
            weSoBack.markUsedUp();
            return weSoBack.fullSolution();
        } else {
            throw new NoSuchElementException();
        }
    }
}
