package pl.wojciechkarpiel.jhou.unifier.simplifier;

import org.junit.jupiter.api.Test;
import pl.wojciechkarpiel.jhou.ast.*;
import pl.wojciechkarpiel.jhou.ast.type.ArrowType;
import pl.wojciechkarpiel.jhou.ast.type.BaseType;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.ast.util.Id;
import pl.wojciechkarpiel.jhou.termHead.BetaEtaNormal;
import pl.wojciechkarpiel.jhou.termHead.HeadOps;
import pl.wojciechkarpiel.jhou.testUtil.TestUtil;
import pl.wojciechkarpiel.jhou.unifier.DisagreementPair;
import pl.wojciechkarpiel.jhou.unifier.DisagreementSet;
import pl.wojciechkarpiel.jhou.unifier.SolutionIterator;
import pl.wojciechkarpiel.jhou.unifier.Unifier;
import pl.wojciechkarpiel.jhou.unifier.simplifier.result.SimplificationResult;
import pl.wojciechkarpiel.jhou.util.ListUtil;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static pl.wojciechkarpiel.jhou.Api.*;

class MatcherTest {
    public static final int MAX_ITER_NONFINDABLE = 6;

    @Test
    void l3l() {
        BaseType c2t = new BaseType(Id.uniqueId());
        Constant c2 = new Constant(Id.uniqueId(), c2t);
        BaseType c1t = new BaseType(Id.uniqueId());
        Constant c1 = new Constant(Id.uniqueId(), c1t);
        Variable v1 = new Variable(Id.uniqueId(), new BaseType(Id.uniqueId()));
        Variable v2 = new Variable(Id.uniqueId(), new BaseType(Id.uniqueId()));
        Variable v = new Variable(Id.uniqueId(), new ArrowType(c1t, new ArrowType(c2t, new BaseType(Id.uniqueId()))));
        Term flexible = new Abstraction(v1, new Abstraction(v2, new Application(new Application(v, c1), c2)));
        Constant c = new Constant(Id.uniqueId(), new ArrowType(c1t, new ArrowType(c2t, new BaseType(Id.uniqueId()))));
        Term rigid = new Abstraction(v1, new Abstraction(v2, new Application(new Application(c, c1), c2)));

        BetaEtaNormal flexBen = BetaEtaNormal.normalize(flexible);
        BetaEtaNormal rigidBen = BetaEtaNormal.normalize(rigid);


        assertThrows(IllegalArgumentException.class, () -> Matcher.match(new Matcher.RigidFlexible(flexBen, flexBen)));
        assertThrows(IllegalArgumentException.class, () -> Matcher.match(new Matcher.RigidFlexible(rigidBen, rigidBen)));
        List<Term> matched = Matcher.match(new Matcher.RigidFlexible(rigidBen, flexBen));
        assertEquals(3, matched.size());
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


        SolutionIterator s = Unifier.unify(left, right, MAX_ITER_NONFINDABLE);
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


        SolutionIterator s = Unifier.unify(left, right, MAX_ITER_NONFINDABLE);
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

        SolutionIterator s = Unifier.unify(left, right, 8);
        TestUtil.assertGoodSolution(s.next(), left, right);
        TestUtil.assertGoodSolution(s.next(), left, right);
        TestUtil.assertGoodSolution(s.next(), left, right);
        TestUtil.assertGoodSolution(s.next(), left, right);
        TestUtil.assertGoodSolution(s.next(), left, right);
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