package pl.wojciechkarpiel.unifier;

import pl.wojciechkarpiel.ast.Term;
import pl.wojciechkarpiel.normalizer.Normalizer;
import pl.wojciechkarpiel.substitution.Substitution;
import pl.wojciechkarpiel.unifier.tree.Tree;
import pl.wojciechkarpiel.unifier.tree.WorkWorkNode;
import pl.wojciechkarpiel.util.ListUtil;

import java.util.ArrayList;

public class Unifier {
    private Unifier() {
    }

    public static SolutionIterator unify(Term a, Term b) {
        return unify(a, b, SolutionIterator.UNLIMITED_ITERATIONS);
    }

    public static SolutionIterator unify(Term a, Term b, int maxIterations) {
        Term na = Normalizer.normalize(a);
        Term nb = Normalizer.normalize(b);
        DisagreementSet ds = new DisagreementSet(ListUtil.of(new DisagreementPair(na, nb)));
        Tree tree = new WorkWorkNode(null, new Substitution(new ArrayList<>()), ds);
        return new SolutionIterator(tree, maxIterations);
    }
}
