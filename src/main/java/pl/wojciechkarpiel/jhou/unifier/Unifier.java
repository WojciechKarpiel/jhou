package pl.wojciechkarpiel.jhou.unifier;

import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.substitution.Substitution;
import pl.wojciechkarpiel.jhou.types.TypeCalculator;
import pl.wojciechkarpiel.jhou.types.inference.TypeInference;
import pl.wojciechkarpiel.jhou.unifier.tree.Tree;
import pl.wojciechkarpiel.jhou.unifier.tree.WorkWorkNode;
import pl.wojciechkarpiel.jhou.util.ListUtil;

import java.util.List;

public class Unifier {
    private Unifier() {
    }

    public static SolutionIterator unify(Term a, Term b) {
        return unify(a, b, new UnificationSettings());
    }

    public static SolutionIterator unify(Term a, Term b, UnificationSettings unificationSettings) {
        List<Term> ab = TypeInference.inferMissing(
                ListUtil.of(a, b),
                unificationSettings.getAllowedTypeInference(),
                unificationSettings.getPrintStream()
        );
        a = ab.get(0);
        b = ab.get(1);
        TypeCalculator.ensureEqualTypes(a, b);
        DisagreementSet disagreementSet = DisagreementSet.of(new DisagreementPair(a, b));
        Tree tree = new WorkWorkNode(null, Substitution.empty(), disagreementSet);
        return new SolutionIterator(tree, unificationSettings.getMaxIterations(), unificationSettings.getPrintStream());
    }
}
