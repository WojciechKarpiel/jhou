package pl.wojciechkarpiel.jhou.unifier.simplifier;

import org.junit.jupiter.api.Test;
import pl.wojciechkarpiel.jhou.ast.*;
import pl.wojciechkarpiel.jhou.ast.type.ArrowType;
import pl.wojciechkarpiel.jhou.ast.type.BaseType;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.ast.util.Id;
import pl.wojciechkarpiel.jhou.normalizer.Normalizer;
import pl.wojciechkarpiel.jhou.substitution.Substitution;
import pl.wojciechkarpiel.jhou.termHead.BetaEtaNormal;
import pl.wojciechkarpiel.jhou.termHead.HeadOps;
import pl.wojciechkarpiel.jhou.testUtil.TestUtil;
import pl.wojciechkarpiel.jhou.types.TypeCalculator;
import pl.wojciechkarpiel.jhou.unifier.*;
import pl.wojciechkarpiel.jhou.unifier.simplifier.result.SimplificationResult;
import pl.wojciechkarpiel.jhou.util.ListUtil;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static pl.wojciechkarpiel.jhou.Api.*;

class MatcherTest {
    public static final int MAX_ITER_NONFINDABLE = 5;

    @Test
    void multiP() {
        Type t = freshType("t");
        Type a = freshType("a");
        Type b = freshType("b");
        Type c = freshType("c");
        Type d = freshType("d");
        Type targetT = arrow(
                arrow(a, arrow(b, c)),
                arrow(
                        arrow(b, arrow(c, d)),
                        t));
        Constant C = (Constant) freshConstant(targetT, "C");
        Variable v = (Variable) freshVariable(targetT, "V");
        Constant arg1 = (Constant) freshConstant(arrow(a, arrow(b, c)), "arg1");
        Constant arg2 = (Constant) freshConstant(arrow(b, arrow(c, d)), "arg2");
        Variable va1 = (Variable) freshVariable(arrow(a, arrow(b, c)), "va1");
        Variable va2 = (Variable) freshVariable(arrow(b, arrow(c, d)), "va2");


        Term left = app(app(C, arg1), arg2);
        Term right = app(app(v, va1), va2);

        TypeCalculator.ensureEqualTypes(left, right);

        SolutionIterator si = Unifier.unify(left, right);
        assertTrue(si.hasNext());
        for (int i = 0; i < 4; i++) {
            Substitution next = si.next();
            System.out.println(i);
            System.out.println(Normalizer.etaContract(Normalizer.betaNormalize(next.substitute(v))));
            System.out.println(Normalizer.etaContract(Normalizer.betaNormalize(next.substitute(va1))));
            System.out.println(Normalizer.etaContract(Normalizer.betaNormalize(next.substitute(va2))));
            TestUtil.assertGoodSolution(next, left, right);
        }
        assertFalse(si.hasNext());
    }

    @Test
    void multiP2() {
        Type t = freshType("t");
        Type a = freshType("a");
        Type b = freshType("b");
        Type c = freshType("c");
        Type d = freshType("d");
        Type targetT = arrow(
                arrow(a, arrow(b, c)),
                arrow(
                        arrow(b, arrow(c, d)),
                        t));
        Constant C = (Constant) freshConstant(targetT, "C");
        Variable v = freshVariable(arrow(d, targetT), "V");
        Constant arg1 = (Constant) freshConstant(arrow(a, arrow(b, c)), "arg1");
        Constant arg2 = (Constant) freshConstant(arrow(b, arrow(c, d)), "arg2");
        Variable va1 = freshVariable(arrow(a, arrow(b, c)), "va1");
        Variable va2 = freshVariable(arrow(b, arrow(c, d)), "va2");


        Term left = app(app(C, arg1), arg2);
        Term right = app(app(app(v, freshConstant(d, "DD")), va1), va2);

        TypeCalculator.ensureEqualTypes(left, right);

        SolutionIterator si = Unifier.unify(left, right);
        assertTrue(si.hasNext());
        for (int i = 0; i < 4; i++) {
            TestUtil.assertGoodSolution(si.next(), left, right);
        }
        assertFalse(si.hasNext());
    }

    @Test
    void l3l() {
        BaseType c2t = new BaseType(Id.uniqueId(), "T2");
        Constant c2 = new Constant(Id.uniqueId(), c2t);
        BaseType c1t = new BaseType(Id.uniqueId(), "T1");
        Constant c1 = new Constant(Id.uniqueId(), c1t);
        Variable v1 = new Variable(Id.uniqueId(), c1t);
        Variable v2 = new Variable(Id.uniqueId(), c2t);
        BaseType fint = new BaseType(Id.uniqueId(), "TFIN");
        Variable v = new Variable(Id.uniqueId(), new ArrowType(c1t, new ArrowType(c2t, c2t)));
        Term flexible = new Abstraction(v1, new Abstraction(v2, new Application(new Application(v, c1), c2)));
        Constant c = new Constant(Id.uniqueId(), new ArrowType(c1t, new ArrowType(c2t, c2t)));
        Term rigid = new Abstraction(v1, new Abstraction(v2, new Application(new Application(c, c1), c2)));

        BetaEtaNormal flexBen = BetaEtaNormal.normalize(flexible);
        BetaEtaNormal rigidBen = BetaEtaNormal.normalize(rigid);


        assertThrows(IllegalArgumentException.class, () -> Matcher.match(new Matcher.RigidFlexible(flexBen, flexBen)));
        assertThrows(IllegalArgumentException.class, () -> Matcher.match(new Matcher.RigidFlexible(rigidBen, rigidBen)));
        List<Term> matched = Matcher.match(new Matcher.RigidFlexible(rigidBen, flexBen));
        assertEquals(2, matched.size()); // one rejected because of mismatch
        {
            BetaEtaNormal impersonation = BetaEtaNormal.normalize(matched.get(0));
            Optional<Constant> co = HeadOps.asConstant(impersonation.getHead());
            assertTrue(co.isPresent());
            assertEquals(c, co.get());
            // todo check binders and args

        }
    }

    //
    @Test
    void exampleFromPaper() {
        Type t = freshType();
        Term D = freshConstant(t, "D");
        Term g = freshVariable(arrow(t, t), "g");
        Term C = freshConstant(arrow(arrow(arrow(t, t), t), t), "C");
        Term y = freshVariable(arrow(t, arrow(arrow(t, t), t)), "y");
        Term left =
                abstraction(t, "x1l", x1 ->
                        abstraction(t, "x2l", x2 ->
                                app(
                                        app(y, D),
                                        abstraction(t, "x3l", x3 -> app(g, x3))
                                )));
        Term right =
                abstraction(t, "x1r", x1 ->
                        abstraction(t, "x2r", x2 ->
                                app(
                                        C,
                                        abstraction(arrow(t, t), "x3r", x3 -> app(x3, x2))
                                )));
        Type eftType = typeOf(left);
        Type rightType = typeOf(right);
        assertEquals(eftType, rightType);

        List<Term> match = Matcher.match(new Matcher.RigidFlexible(BetaEtaNormal.normalize(right), BetaEtaNormal.normalize(left)));
        assertEquals(3, match.size());
        Term imitator = match.get(0);
        {
            BetaEtaNormal imn = BetaEtaNormal.normalize(imitator);
            assertEquals(C, imn.getHead().getTerm());
            assertEquals(2, imn.getBinder().size());
            assertEquals(1, imn.getArguments().size()); // todo paper example seems wrong q=1
            // TODO inspect arguments
        }
        {
            Term proj1 = match.get(1);
            BetaEtaNormal b1 = BetaEtaNormal.normalize(proj1);
            assertEquals(2, b1.getBinder().size());
            assertEquals(0, b1.getArguments().size());
            assertEquals(b1.getBinder().get(0), b1.getHead().getTerm());
        }

        {
            Term proj2 = match.get(2);
            BetaEtaNormal b1 = BetaEtaNormal.normalize(proj2);
            assertEquals(2, b1.getBinder().size());
            assertEquals(1, b1.getArguments().size());
            assertEquals(b1.getBinder().get(1), b1.getHead().getTerm());
        }

        UnificationSettings us = new UnificationSettings(MAX_ITER_NONFINDABLE);
        SolutionIterator s = Unifier.unify(left, right, us);
        assertFalse(s.hasNext());
    }


    @Test
    void exampleFromPaper2() {
        Type t = freshType();
        Term D = freshConstant(t, "D");
        Term g = freshVariable(arrow(t, t), "g");
        Term C = freshConstant(arrow(arrow(t, t), arrow(t, t)), "C");
        Term y = freshVariable(arrow(t, arrow(arrow(t, t), t)), "y");
        Term left =
                abstraction(t, "x1l", x1 ->
                        abstraction(t, "x2l", x2 ->
                                app(
                                        app(y, D),
                                        abstraction(t, "x3l", x3 -> app(g, x3))
                                )));
        Term right =
                abstraction(t, "x1r", x1 ->
                        abstraction(t, "x2r", x2 ->
                                app(
                                        app(
                                                C,
                                                abstraction(t, "x3r", x3 -> x3)
                                        ),
                                        x2)));
        Type eftType = typeOf(left);
        Type rightType = typeOf(right);
        assertEquals(eftType, rightType);

        BetaEtaNormal noRight = BetaEtaNormal.normalize(right);
        BetaEtaNormal noLeft = BetaEtaNormal.normalize(left);
        SimplificationResult q = Simplifier.simplify(new DisagreementSet(ListUtil.of(new DisagreementPair(left, right))));
        List<Term> match = Matcher.match(new Matcher.RigidFlexible(noRight, noLeft));
        assertEquals(3, match.size());
        Term imitator = match.get(0);
        {
            BetaEtaNormal imn = BetaEtaNormal.normalize(imitator);
            assertEquals(C, imn.getHead().getTerm());
            assertEquals(2, imn.getBinder().size());
            assertEquals(2, imn.getArguments().size());
        }
        {
            Term proj1 = match.get(1);
            BetaEtaNormal b1 = BetaEtaNormal.normalize(proj1);
            assertEquals(2, b1.getBinder().size());
            assertEquals(0, b1.getArguments().size());
            assertEquals(b1.getBinder().get(0), b1.getHead().getTerm());
        }

        {
            Term proj2 = match.get(2);
            BetaEtaNormal b1 = BetaEtaNormal.normalize(proj2);
            assertEquals(2, b1.getBinder().size());
            assertEquals(1, b1.getArguments().size());
            assertEquals(b1.getBinder().get(1), b1.getHead().getTerm());
        }


        UnificationSettings us = new UnificationSettings(MAX_ITER_NONFINDABLE);
        SolutionIterator s = Unifier.unify(left, right, us);
        assertFalse(s.hasNext());
    }


    @Test
    void exampleFromPaper2solvable() {
        Type t = freshType();
        Term D = freshConstant(t, "D");
        Term g = freshVariable(arrow(t, t), "g");
        Term C = freshConstant(arrow(arrow(t, t), arrow(t, t)), "C");
        Term y = freshVariable(arrow(t, arrow(arrow(t, t), t)), "y");
        Term left =
                abstraction(t, "x1l", x1 ->
                        abstraction(t, "x2l", x2 ->
                                app(
                                        app(y, D),
                                        abstraction(t, "x3l", x3 -> app(g, x2))
                                )));
        Term right =
                abstraction(t, "x1r", x1 ->
                        abstraction(t, "x2r", x2 ->
                                app(
                                        app(
                                                C,
                                                abstraction(t, "x3r", x3 -> x3)
                                        ),
                                        x2)));


        UnificationSettings us = new UnificationSettings(8);
        SolutionIterator s = Unifier.unify(left, right, us);
        for (int i = 0; i < 2; i++) {
            Substitution next = s.next();
            TestUtil.assertGoodSolution(next, left, right);
        }
        assertFalse(s.hasNext());
    }

    @Test
    void exampleFromPaperSolvable() {
        Type t = freshType();
        Term g = freshVariable(arrow(t, t), "g");
        Term C = freshConstant(arrow(arrow(arrow(t, t), t), t), "C");
        Term y = freshVariable(arrow(t, arrow(arrow(t, t), t)), "y");
        Term left =
                abstraction(t, "x1l", x1 ->
                        abstraction(t, "x2l", x2 ->
                                app(
                                        app(y, x2),
                                        abstraction(t, "x3l", x3 -> app(g, x3))
                                )));
        Term right =
                abstraction(t, "x1r", x1 ->
                        abstraction(t, "x2r", x2 ->
                                app(
                                        C,
                                        abstraction(arrow(t, t), "x3r", x3 -> app(x3, x2))
                                )));


        SolutionIterator s = Unifier.unify(left, right);
        TestUtil.assertGoodSolution(s.next(), left, right);
    }
}