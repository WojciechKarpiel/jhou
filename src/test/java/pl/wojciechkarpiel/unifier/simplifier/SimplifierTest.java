package pl.wojciechkarpiel.unifier.simplifier;

import org.junit.jupiter.api.Test;
import pl.wojciechkarpiel.ast.Abstraction;
import pl.wojciechkarpiel.ast.Constant;
import pl.wojciechkarpiel.ast.Term;
import pl.wojciechkarpiel.ast.Variable;
import pl.wojciechkarpiel.ast.type.BaseType;
import pl.wojciechkarpiel.ast.type.Type;
import pl.wojciechkarpiel.ast.util.Id;
import pl.wojciechkarpiel.unifier.DisagreementPair;
import pl.wojciechkarpiel.unifier.DisagreementSet;
import pl.wojciechkarpiel.unifier.simplifier.result.SimplificationResult;
import pl.wojciechkarpiel.unifier.simplifier.result.SimplificationSuccess;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplifierTest {

    @Test
    void unifyConstant() {
        Constant c = new Constant(Id.uniqueId(), new BaseType(Id.uniqueId()));

        List<DisagreementPair> l = new ArrayList<>();
        l.add(new DisagreementPair(c, c));
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
        l.add(new DisagreementPair(t1, t2));
        SimplificationResult r = Simplifier.simplify(new DisagreementSet(l));
        assertInstanceOf(SimplificationSuccess.class, r);
        SimplificationSuccess s = (SimplificationSuccess) r;
        assertTrue(s.getSolution().getSubstitution().isEmpty());
    }
}