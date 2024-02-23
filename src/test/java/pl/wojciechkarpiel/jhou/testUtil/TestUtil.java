package pl.wojciechkarpiel.jhou.testUtil;

import pl.wojciechkarpiel.jhou.Api;
import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.ast.Variable;
import pl.wojciechkarpiel.jhou.ast.util.FreeVariable;
import pl.wojciechkarpiel.jhou.substitution.Substitution;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static pl.wojciechkarpiel.jhou.Api.betaNormalEtaContracted;

public class TestUtil {

    public static void assertGoodSolution(Substitution s, Term a, Term b) {
        Term as = s.substitute(a);
        Term bs = s.substitute(b);
        assertAlphaEqual(Api.betaNormalEtaContracted(as), betaNormalEtaContracted(bs));
        Set<Variable> freeA = FreeVariable.getFreeVariables(as);
        Set<Variable> freeB = FreeVariable.getFreeVariables(bs);

        if (!freeA.isEmpty() || !freeB.isEmpty()) {
            fail();
        }

        // this in unnecessary, implied by above
        Set<Variable> origAfv = FreeVariable.getFreeVariables(a);
        Set<Variable> origBfv = FreeVariable.getFreeVariables(b);
        freeB.removeAll(origBfv);
        freeA.removeAll(origBfv);
        freeB.removeAll(origAfv);
        freeA.removeAll(origAfv);
        assertTrue(freeA.isEmpty());
        assertTrue(freeB.isEmpty());
    }

    public static void assertAlphaEqual(Term a, Term b) {
        assertTrue(Api.alphaEqual(a, b));
    }

    public static void assertAlphaEqual(Substitution a, Substitution b) {
        assertTrue(a.alphaEquals(b));
    }

    public static void assertNotAlphaEqual(Term a, Term b) {
        assertFalse(Api.alphaEqual(a, b));
    }

}
