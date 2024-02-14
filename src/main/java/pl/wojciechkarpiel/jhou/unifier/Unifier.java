package pl.wojciechkarpiel.jhou.unifier;

import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.normalizer.Normalizer;
import pl.wojciechkarpiel.jhou.substitution.Substitution;
import pl.wojciechkarpiel.jhou.types.TypeCalculator;
import pl.wojciechkarpiel.jhou.types.TypeMismatchException;
import pl.wojciechkarpiel.jhou.unifier.tree.Tree;
import pl.wojciechkarpiel.jhou.unifier.tree.WorkWorkNode;
import pl.wojciechkarpiel.jhou.util.ListUtil;

import java.util.ArrayList;

public class Unifier {
    private Unifier() {
    }

    public static SolutionIterator unify(Term a, Term b) {
        return unify(a, b, SolutionIterator.UNLIMITED_ITERATIONS);
    }

    public static SolutionIterator unify(Term a, Term b, int maxIterations) {
        Type at = TypeCalculator.calculateType(a);
        Type bt = TypeCalculator.calculateType(b);
        if (!at.equals(bt)) throw new TypeMismatchException("");

        // todo how to get eta here without breaking stuff?
        Term na = Normalizer.betaNormalize(a);
        Term nb = Normalizer.betaNormalize(b);
        DisagreementSet ds = new DisagreementSet(ListUtil.of(new DisagreementPair(na, nb)));
        Tree tree = new WorkWorkNode(null, new Substitution(new ArrayList<>()), ds);
        return new SolutionIterator(tree, maxIterations);
    }
}
