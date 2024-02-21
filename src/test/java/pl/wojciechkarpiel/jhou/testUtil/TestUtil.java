package pl.wojciechkarpiel.jhou.testUtil;

import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.substitution.Substitution;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static pl.wojciechkarpiel.jhou.Api.alphaBetaEtaEqual;

public class TestUtil {

    public static void assertGoodSolution(Substitution s, Term a, Term b) {
        Term as = s.substitute(a);
        Term bs = s.substitute(b);
        assertTrue(alphaBetaEtaEqual(as, bs));
        // TODO: check that there are no free variables!
    }
}
