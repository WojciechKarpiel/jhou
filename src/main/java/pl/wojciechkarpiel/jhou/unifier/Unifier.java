package pl.wojciechkarpiel.jhou.unifier;

import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.types.TypeCalculator;
import pl.wojciechkarpiel.jhou.types.inference.TypeInference;
import pl.wojciechkarpiel.jhou.unifier.tree.Tree;
import pl.wojciechkarpiel.jhou.unifier.tree.WorkWorkNode;
import pl.wojciechkarpiel.jhou.util.Pair;

public class Unifier {
    private Unifier() {
    }

    public static SolutionIterator unify(Term a, Term b) {
        return unify(a, b, new UnificationSettings());
    }

    public static SolutionIterator unify(Term a, Term b, UnificationSettings unificationSettings) {
        Pair<Term, Term> ab = TypeInference.inferMissing(
                Pair.of(a, b),
                unificationSettings.getAllowedTypeInference(),
                unificationSettings.getPrintStream()
        );
        TypeCalculator.ensureEqualTypes(ab.getLeft(), ab.getRight());
        Tree tree = WorkWorkNode.searchTreeRoot(ab.getLeft(), ab.getRight());
        return new SolutionIterator(tree, unificationSettings.getMaxIterations(), unificationSettings.getPrintStream());
    }
}
