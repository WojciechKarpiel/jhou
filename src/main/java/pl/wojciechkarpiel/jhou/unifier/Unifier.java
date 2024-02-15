package pl.wojciechkarpiel.jhou.unifier;

import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.substitution.Substitution;
import pl.wojciechkarpiel.jhou.types.TypeCalculator;
import pl.wojciechkarpiel.jhou.unifier.tree.Tree;
import pl.wojciechkarpiel.jhou.unifier.tree.WorkWorkNode;

public class Unifier {
    private Unifier() {
    }

    public static SolutionIterator unify(Term a, Term b) {
        return unify(a, b, SolutionIterator.UNLIMITED_ITERATIONS);
    }

    public static SolutionIterator unify(Term a, Term b, int maxIterations) {
        TypeCalculator.ensureEqualTypes(a, b);
        DisagreementSet disagreementSet = DisagreementSet.of(new DisagreementPair(a, b));
        Tree tree = new WorkWorkNode(null, Substitution.empty(), disagreementSet);
        return new SolutionIterator(tree, maxIterations);
    }
}
