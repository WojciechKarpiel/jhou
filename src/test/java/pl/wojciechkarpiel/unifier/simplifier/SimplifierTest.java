package pl.wojciechkarpiel.unifier.simplifier;

import org.junit.jupiter.api.Test;
import pl.wojciechkarpiel.ast.*;
import pl.wojciechkarpiel.ast.type.ArrowType;
import pl.wojciechkarpiel.ast.type.BaseType;
import pl.wojciechkarpiel.ast.type.Type;
import pl.wojciechkarpiel.ast.util.Id;
import pl.wojciechkarpiel.termHead.BetaEtaNormal;
import pl.wojciechkarpiel.unifier.DisagreementPair;
import pl.wojciechkarpiel.unifier.DisagreementSet;
import pl.wojciechkarpiel.unifier.PairType;
import pl.wojciechkarpiel.unifier.simplifier.result.NonUnifiable;
import pl.wojciechkarpiel.unifier.simplifier.result.SimplificationResult;
import pl.wojciechkarpiel.unifier.simplifier.result.SimplificationSuccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SimplifierTest {

    @Test
    void unifyConstant() {
        Constant c = new Constant(Id.uniqueId(), new BaseType(Id.uniqueId()));

        List<DisagreementPair> l = new ArrayList<>();
        l.add(new DisagreementPair(BetaEtaNormal.normalize(c), BetaEtaNormal.normalize(c)));
        SimplificationResult r = Simplifier.simplify(new DisagreementSet(l));
        assertInstanceOf(SimplificationSuccess.class, r);
        SimplificationSuccess s = (SimplificationSuccess) r;
        assertTrue(s.getSolution().getSubstitution().isEmpty());
    }

    @Test
    void unifylamxx() {
        Type tpe = new BaseType(Id.uniqueId());
        Variable v1 = new Variable(Id.uniqueId(), tpe);
        Variable v2 = new Variable(Id.uniqueId(), tpe);
        Term t1 = new Abstraction(v1, v1);
        Term t2 = new Abstraction(v2, v2);
        List<DisagreementPair> l = new ArrayList<>();
        l.add(new DisagreementPair(BetaEtaNormal.normalize(t1), BetaEtaNormal.normalize(t2)));
        SimplificationResult r = Simplifier.simplify(new DisagreementSet(l));
        assertInstanceOf(SimplificationSuccess.class, r);
        SimplificationSuccess s = (SimplificationSuccess) r;
        assertTrue(s.getSolution().getSubstitution().isEmpty());
    }


    /**
     * EXAMPLE JUST ABOVE SECTION 3.5 IN THE PAPER
     */
    @Test
    void breakDownRigidRigid() {
        Type argT = new BaseType(Id.uniqueId());
        Type otherT = new BaseType(Id.uniqueId());
        Type ar = new ArrowType(argT, otherT);

        Constant c = new Constant(Id.uniqueId(), ar);
        Constant a = new Constant(Id.uniqueId(), otherT);
        Constant d = new Constant(Id.uniqueId(), otherT);
        Variable x1 = new Variable(Id.uniqueId(), argT);
        Variable x2 = new Variable(Id.uniqueId(), argT);


        Term left = new Abstraction(x1, new Application(new Application(c, x1), d));
        Term right = new Abstraction(x2, new Application(new Application(c, x2), a));

        BetaEtaNormal aq =
                BetaEtaNormal.normalize(left);
        BetaEtaNormal bq = BetaEtaNormal.normalize(right);


        Optional<List<DisagreementPair>> r = Simplifier.breakdownRigidRigid(aq, bq);
        assertTrue(r.isPresent());
        assertEquals(2, r.get().size());


        {
            // first is \x.x
            DisagreementPair fdp = r.get().get(0);
            assertEquals(PairType.RIGID_RIGID, fdp.getType());
            // This can break and x2 could be instead!!!!
            assertEquals(x1, fdp.getMostRigid().getHead().getTerm());
            assertEquals(x1, fdp.getLeastRigid().getHead().getTerm());
            List<Variable> p1 = new ArrayList<>();
            p1.add(x1);
            assertEquals(p1, fdp.getMostRigid().getBinder());
            assertEquals(p1, fdp.getLeastRigid().getBinder());
            assertTrue(fdp.getLeastRigid().getArguments().isEmpty());
            assertTrue(fdp.getMostRigid().getArguments().isEmpty());
        }
        {
            // second is \x.C,\x.D
            DisagreementPair fdp = r.get().get(1);
            assertEquals(PairType.RIGID_RIGID, fdp.getType());
            // This can break and x2 could be instead!!!!
            assertEquals(d, fdp.getMostRigid().getHead().getTerm());
            assertEquals(a, fdp.getLeastRigid().getHead().getTerm());
            List<Variable> p1 = new ArrayList<>();
            p1.add(x1);
            assertEquals(p1, fdp.getMostRigid().getBinder());
            assertEquals(p1, fdp.getLeastRigid().getBinder());
            assertTrue(fdp.getLeastRigid().getArguments().isEmpty());
            assertTrue(fdp.getMostRigid().getArguments().isEmpty());
        }


        DisagreementPair p1 = r.get().get(0);
        Optional<List<DisagreementPair>> r0 = Simplifier.breakdownRigidRigid(p1.getLeastRigid(), p1.getMostRigid());
        assertTrue(r0.isPresent());
        assertTrue(r0.get().isEmpty());


        DisagreementPair p2 = r.get().get(1);
        Optional<List<DisagreementPair>> r1 = Simplifier.breakdownRigidRigid(p2.getLeastRigid(), p2.getMostRigid());
        assertFalse(r1.isPresent());


        // now do the same, but in one sweep
        {
            List<DisagreementPair> dp = new ArrayList<>();
            dp.add(new DisagreementPair(left, right));
            SimplificationResult rr = Simplifier.simplify(new DisagreementSet(dp));
            assertEquals(NonUnifiable.INSTANCE, rr);
        }
    }
}