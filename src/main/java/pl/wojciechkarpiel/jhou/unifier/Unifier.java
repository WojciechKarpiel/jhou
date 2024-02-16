package pl.wojciechkarpiel.jhou.unifier;

import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.substitution.Substitution;
import pl.wojciechkarpiel.jhou.types.TypeCalculator;
import pl.wojciechkarpiel.jhou.unifier.tree.Tree;
import pl.wojciechkarpiel.jhou.unifier.tree.WorkWorkNode;

import java.io.PrintStream;

public class Unifier {
    private Unifier() {
    }

    public static SolutionIterator unify(Term a, Term b) {
        return unify(a, b, SolutionIterator.UNLIMITED_ITERATIONS, SolutionIterator.DEFAULT_PRINT_STREAM);
    }

    public static SolutionIterator unify(Term a, Term b, int maxIterations) {
        return unify(a, b, maxIterations, SolutionIterator.DEFAULT_PRINT_STREAM);
    }

    public static SolutionIterator unify(Term a, Term b, PrintStream printStream) {
        return unify(a, b, SolutionIterator.UNLIMITED_ITERATIONS, printStream);
    }

    public static SolutionIterator unify(Term a, Term b, int maxIterations, PrintStream printStream) {
        TypeCalculator.ensureEqualTypes(a, b);
        DisagreementSet disagreementSet = DisagreementSet.of(new DisagreementPair(a, b));
        Tree tree = new WorkWorkNode(null, Substitution.empty(), disagreementSet);
        return new SolutionIterator(tree, maxIterations, printStream);
    }
}
