package pl.wojciechkarpiel.unifier;

import org.junit.jupiter.api.Test;
import pl.wojciechkarpiel.ast.*;
import pl.wojciechkarpiel.ast.type.ArrowType;
import pl.wojciechkarpiel.ast.type.BaseType;
import pl.wojciechkarpiel.ast.type.Type;
import pl.wojciechkarpiel.ast.util.Id;
import pl.wojciechkarpiel.substitution.Substitution;

import static org.junit.jupiter.api.Assertions.*;

class UnifierTest {

    @Test
    void unifyLamxx() {
        BaseType type = new BaseType(Id.uniqueId());
        Variable x = new Variable(Id.uniqueId(), type);
        Variable y = new Variable(Id.uniqueId(), type);
        Substitution s = Unifier.unify(new Abstraction(x, x), new Abstraction(y, y));
        assertTrue(s.getSubstitution().isEmpty());
    }

    @Test
    void nagmiConstants() {
        Type t = new BaseType(Id.uniqueId());
        Term a = new Constant(Id.uniqueId(), t);
        Term b = new Constant(Id.uniqueId(), t);
        assertThrows(NagmiException.class, () -> Unifier.unify(a, b));
    }

    @Test
    void fromPaper35() {
        Type xT = new BaseType(Id.uniqueId());
        Type yT = new ArrowType(xT, xT);
        Type cT = yT;
        Constant c = new Constant(Id.uniqueId(), cT, "C");

        Term left;
        {
            Variable x = new Variable(Id.uniqueId(), xT, "xL");
            Variable y = new Variable(Id.uniqueId(), yT, "y");
            left = new Abstraction(x, new Application(y, new Application(c, new Application(y, x))));
        }
        Term right;
        {
            Variable x = new Variable(Id.uniqueId(), xT, "xR");
            right = new Abstraction(x, new Application(c, x));
        }
        Substitution s = Unifier.unify(left, right);
        assertEquals(1, s.getSubstitution().size());
        // TODO assert y -> lam(x).x
    }


}