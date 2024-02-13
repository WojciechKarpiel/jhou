package pl.wojciechkarpiel.unifier;

import pl.wojciechkarpiel.ast.Term;
import pl.wojciechkarpiel.normalizer.Normalizer;
import pl.wojciechkarpiel.substitution.Substitution;
import pl.wojciechkarpiel.unifier.tree.Tree;
import pl.wojciechkarpiel.unifier.tree.WeBackNode;
import pl.wojciechkarpiel.unifier.tree.WorkWorkNode;
import pl.wojciechkarpiel.util.ListUtil;

import java.util.ArrayList;
import java.util.Optional;

public class Unifier {
    private Unifier() {
    }

    public static final int DEFAULT_MAX_ITERATIONS = 8;

    public static Substitution unify(Term a, Term b) {
        return unify(a, b, DEFAULT_MAX_ITERATIONS);
    }

    public static Substitution unify(Term a, Term b, int maxIterations) {
        Term na = Normalizer.normalize(a);
        Term nb = Normalizer.normalize(b);
        DisagreementSet ds = new DisagreementSet(ListUtil.of(new DisagreementPair(na, nb)));
        Tree tree = new WorkWorkNode(null, new Substitution(new ArrayList<>()), ds);

        for (int i = 0; i < maxIterations; i++) {
            System.out.println("Attempt: " + i);
            tree.expandOnce();
            if (tree.itsOver()) {
                System.out.println("It's over");
                throw new NagmiException();
            }
            Optional<WeBackNode> weBack = tree.weBack();
            if (weBack.isPresent()) {
                System.out.println("WE BACK");
                Substitution solution = weBack.get().fullSolution();
                System.out.println(solution);
                return solution;
            }
        }
        throw new NoSolutionFoundException();
    }
}
