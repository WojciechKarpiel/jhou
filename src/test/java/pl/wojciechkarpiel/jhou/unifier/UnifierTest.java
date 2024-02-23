package pl.wojciechkarpiel.jhou.unifier;

import org.junit.jupiter.api.Test;
import pl.wojciechkarpiel.jhou.ast.*;
import pl.wojciechkarpiel.jhou.ast.type.ArrowType;
import pl.wojciechkarpiel.jhou.ast.type.BaseType;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.ast.util.Id;
import pl.wojciechkarpiel.jhou.substitution.Substitution;
import pl.wojciechkarpiel.jhou.substitution.SubstitutionPair;
import pl.wojciechkarpiel.jhou.testUtil.TestUtil;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static pl.wojciechkarpiel.jhou.Api.*;

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
        TestUtil.assertAlphaEqual(lamxx, sub.getTerm());
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

        assertTrue(alphaBetaEtaEqual(left, right));
        assertFalse(alphaEqual(left, right));
    }


    @Test
    void unifyLam() {
        Type t = freshType();
        Term lamxx = abstraction(t, x -> x);
        Variable v = freshVariable(arrow(t, t), "v");
        SolutionIterator it = unify(lamxx, v);

        TestUtil.assertAlphaEqual(new Substitution(v, lamxx), it.next());
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

        System.out.println(solutions.next());
        System.out.println(solutions.next());
        System.out.println(solutions.next());
        System.out.println(solutions.next());
        assertFalse(solutions.hasNext());
    }

    @Test
    void tryVoodo() {
        // looking for solution y -> fn z1 z2. z2 (lam z1z2.z1)

        Type t = freshType();
        Variable y = freshVariable(arrow(t, arrow(arrow(t, t), t)), "y");
        Term c = freshConstant(t, "C");
        Term l3l = app(app(y, c), abstraction(t, x -> x));

        SolutionIterator s = Unifier.unify(l3l, c);
        s.next();
        s.next();
        s.next();

        // now the one I've been looking for <3
        Substitution beautiful = s.next();
        TestUtil.assertGoodSolution(beautiful, l3l, c);
        // `beautiful` is Substitution{[{y → λV_8.λV_9.(V_9 ((V_10 V_8) V_9))}, {V_10 → λV_13.λV_14.V_13}]}

        assertEquals(2, beautiful.getSubstitution().size());
        SubstitutionPair snd = beautiful.getSubstitution().get(1);

        Variable v10v = freshVariable(arrow(t, arrow(arrow(t, t), t)), "v10");
        Term v10t = abstraction(t, a1 -> abstraction(arrow(t, t), a2 -> a1));
        assertEquals(v10v.getType(), typeOf(v10t));
        assertEquals(snd.getVariable().getType(), v10v.getType());
        TestUtil.assertAlphaEqual(snd.getTerm(), v10t);

        Term expectedReplacement = abstraction(t, "z1", z1 ->
                abstraction(arrow(t, t), "z2", z2 ->
                        app(z2, app(app(snd.getVariable(), z1), z2))));
        assertEquals(typeOf(expectedReplacement), typeOf(y));
        TestUtil.assertAlphaEqual(expectedReplacement, beautiful.getSubstitution().get(0).getTerm());
    }

    @Test
    void vacuousUnif() {
        {
            Type t = freshType("T");
            Variable v = freshVariable(t, "v");
            SolutionIterator si = unify(v, v);
            assertTrue(si.hasNext());
            Substitution s = si.next();
            assertFalse(si.hasNext());
            assertInstanceOf(Constant.class, s.substitute(v));
            TestUtil.assertGoodSolution(s, v, v);
        }
        {
            // same but with infetence
            Variable v = freshVariable("v");
            SolutionIterator si = unify(v, v);

            assertTrue(si.hasNext());
            Substitution s;
            s = si.next();
            assertFalse(si.hasNext());
            TestUtil.assertGoodSolution(s, v, v);
            assertInstanceOf(Constant.class, s.substitute(v));
            assertEquals(v.getId(), s.regenerateType(v).getId());
        }
    }

    @Test
    void almostvacuousUnif() {
        {
            Type t = freshType("T");
            Variable v = freshVariable(t, "v");
            Variable w = freshVariable(t, "w");
            SolutionIterator si = unify(v, w);
            assertTrue(si.hasNext());
            Optional<Substitution> so = si.peek();
            Substitution s = si.next();
            assertTrue(so.isPresent());
            assertEquals(s, so.get());
            assertFalse(si.hasNext());
            assertInstanceOf(Constant.class, s.substitute(v));
            assertInstanceOf(Constant.class, s.substitute(w));
            TestUtil.assertGoodSolution(s, v, w);
        }
        {
            // same but with infetence
            Variable v = freshVariable("v");
            Variable w = freshVariable("w");
            SolutionIterator si = unify(v, w);

            assertTrue(si.hasNext());
            Substitution s;
            s = si.next();
            assertFalse(si.hasNext());
            TestUtil.assertGoodSolution(s, v, w);

            assertInstanceOf(Constant.class, s.substitute(v));
            assertInstanceOf(Constant.class, s.substitute(w));
            assertEquals(v.getId(), s.regenerateType(v).getId());
            assertEquals(w.getId(), s.regenerateType(w).getId());
        }

        {
            // same but with PARTIAL-left infetence
            Type t = freshType("T");
            Variable v = freshVariable(t, "v");
            Variable w = freshVariable("w");
            SolutionIterator si = unify(v, w);

            assertTrue(si.hasNext());
            Substitution s;
            s = si.next();
            assertFalse(si.hasNext());
            assertInstanceOf(Constant.class, s.substitute(v));
            assertInstanceOf(Constant.class, s.substitute(w));
            TestUtil.assertGoodSolution(s, v, w);
            assertEquals(v.getId(), s.regenerateType(v).getId());
            assertEquals(v, s.regenerateType(v));
            assertEquals(w.getId(), s.regenerateType(w).getId());
        }
        {
            // same but with PARTIAL-right infetence
            Type t = freshType("T");
            Variable v = freshVariable("v");
            Variable w = freshVariable(t, "w");
            SolutionIterator si = unify(v, w);

            assertTrue(si.hasNext());
            Substitution s;
            s = si.next();
            assertFalse(si.hasNext());
            TestUtil.assertGoodSolution(s, v, w);
            assertEquals(v.getId(), s.regenerateType(v).getId());
            assertEquals(w, s.regenerateType(w));
            assertEquals(w.getId(), s.regenerateType(w).getId());
        }
    }
}