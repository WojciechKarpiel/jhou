package pl.wojciechkarpiel.jhou.unifier.simplifier;

import org.junit.jupiter.api.Test;
import pl.wojciechkarpiel.jhou.ast.*;
import pl.wojciechkarpiel.jhou.ast.type.ArrowType;
import pl.wojciechkarpiel.jhou.ast.type.BaseType;
import pl.wojciechkarpiel.jhou.ast.util.Id;
import pl.wojciechkarpiel.jhou.termHead.BetaEtaNormal;
import pl.wojciechkarpiel.jhou.termHead.HeadOps;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MatcherTest {

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
}