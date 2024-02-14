package pl.wojciechkarpiel.jhou.unifier;

import org.junit.jupiter.api.Test;
import pl.wojciechkarpiel.jhou.ast.*;
import pl.wojciechkarpiel.jhou.ast.type.ArrowType;
import pl.wojciechkarpiel.jhou.ast.type.BaseType;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.ast.util.Id;
import pl.wojciechkarpiel.jhou.substitution.Substitution;
import pl.wojciechkarpiel.jhou.substitution.SubstitutionPair;

import static org.junit.jupiter.api.Assertions.*;
import static pl.wojciechkarpiel.jhou.api.Api.*;

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
        assertEquals(lamxx, sub.getTerm());
    }

    @Test
    void etaAdmitted() {
        Type t = BaseType.freshBaseType();
        Type y = BaseType.freshBaseType();
        Constant f = Constant.freshConstant(new ArrowType(t, y), "F");
        Variable x = Variable.freshVariable(t, "x");

        Term left = new Abstraction(x, new Application(f, x));
        Term right = f;
        SolutionIterator result = Unifier.unify(left, f);

        assertTrue(result.next().getSubstitution().isEmpty());
        assertFalse(result.hasNext());
    }


    @Test
    void unifyLam() {
        Type t = freshType();
        Term lamxx = abstraction(t, x -> x);
        Variable v = freshVariable(arrow(t, t), "v");
        SolutionIterator it = unify(lamxx, v);

        assertEquals(new Substitution(v, lamxx), it.next());
        assertFalse(it.hasNext());
    }

    @Test
    void rnd() {
        Type t = freshType();
        Term plus = freshConstant(arrow(t, arrow(t, t)), "+");
        Term n = freshConstant(t, "n");
        Variable f = freshVariable(arrow(t, t), "f");

        Term left = app(f, n);
        Term right = app(app(plus, n), n);

        SolutionIterator solutions = Unifier.unify(left, right);

        solutions.next();
        solutions.next();
        solutions.next();
        solutions.next();
        solutions.next();
        solutions.next();
        solutions.next();
        solutions.next();
        // TODO it should find 4 solutions, but duplicating each works too
        assertFalse(solutions.hasNext());
    }
}