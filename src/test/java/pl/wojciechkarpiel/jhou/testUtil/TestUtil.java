package pl.wojciechkarpiel.jhou.testUtil;

import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.ast.Variable;
import pl.wojciechkarpiel.jhou.ast.util.FreeVariable;
import pl.wojciechkarpiel.jhou.substitution.Substitution;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static pl.wojciechkarpiel.jhou.Api.alphaBetaEtaEqual;

public class TestUtil {

    public static void assertGoodSolution(Substitution s, Term a, Term b) {
        Term as = s.substitute(a);
        Term bs = s.substitute(b);
        assertTrue(alphaBetaEtaEqual(as, bs));
        Set<Variable> freeA = FreeVariable.getFreeVariables(as);
        Set<Variable> freeB = FreeVariable.getFreeVariables(bs);

        Set<Variable> origAfv = FreeVariable.getFreeVariables(a);
        Set<Variable> origBfv = FreeVariable.getFreeVariables(b);
        // todo not good, all FVs should be eliminated and exchanged for a constant, no leftovers
        if (!freeA.isEmpty() || !freeB.isEmpty()) {
            System.out.println("NO GOOD - LEFT FVs");
        }
        freeB.removeAll(origBfv);
        freeA.removeAll(origBfv);
        freeB.removeAll(origAfv);
        freeA.removeAll(origAfv);
        assertTrue(freeA.isEmpty());
        assertTrue(freeB.isEmpty());
    }
}
