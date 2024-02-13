package pl.wojciechkarpiel.unifier;

import org.junit.jupiter.api.Test;
import pl.wojciechkarpiel.ast.*;
import pl.wojciechkarpiel.ast.type.ArrowType;
import pl.wojciechkarpiel.ast.type.BaseType;
import pl.wojciechkarpiel.ast.type.Type;
import pl.wojciechkarpiel.ast.util.Id;
import pl.wojciechkarpiel.substitution.Substitution;
import pl.wojciechkarpiel.substitution.SubstitutionPair;
import pl.wojciechkarpiel.termHead.HeaderUnifier;

import static org.junit.jupiter.api.Assertions.*;

class UnifierTest {

    @Test
    void unifyLamxx() {
        BaseType type = new BaseType(Id.uniqueId());
        Variable x = new Variable(Id.uniqueId(), type);
        Variable y = new Variable(Id.uniqueId(), type);
        SolutionIterator s = Unifier.unify(new Abstraction(x, x), new Abstraction(y, y));

        assertTrue(s.next().getSubstitution().isEmpty());
        assertFalse(s.hasNext());

    }

    @Test
    void nagmiConstants() {
        Type t = new BaseType(Id.uniqueId());
        Term a = new Constant(Id.uniqueId(), t);
        Term b = new Constant(Id.uniqueId(), t);
        assertFalse(Unifier.unify(a, b).hasNext());
    }

    @Test
    void fromPaper35() {
        Type xT = BaseType.freshBaseType();
        Type yT = new ArrowType(xT, xT);
        Type cT = yT;
        Constant c = new Constant(Id.uniqueId(), cT, "C");
        Variable y = new Variable(Id.uniqueId(), yT, "y");

        Term left;
        {
            Variable x = new Variable(Id.uniqueId(), xT, "xL");
            left = new Abstraction(x, new Application(y, new Application(c, new Application(y, x))));
        }
        Term right;
        {
            Variable x = new Variable(Id.uniqueId(), xT, "xR");
            right = new Abstraction(x, new Application(c, x));
        }
        SolutionIterator si = Unifier.unify(left, right);
        Substitution s = si.next();
        assertFalse(si.hasNext());

        assertEquals(1, s.getSubstitution().size());

        SubstitutionPair sub = s.getSubstitution().get(0);
        assertEquals(y, sub.getVariable());
        Variable fresh = new Variable(Id.uniqueId(), xT);
        Term lamxx = new Abstraction(fresh, fresh);
        assertTrue(HeaderUnifier.headAlphaUnifiable(lamxx, sub.getTerm()));
    }
}