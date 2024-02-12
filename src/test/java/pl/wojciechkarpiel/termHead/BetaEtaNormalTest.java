package pl.wojciechkarpiel.termHead;

import org.junit.jupiter.api.Test;
import pl.wojciechkarpiel.ast.*;
import pl.wojciechkarpiel.ast.type.ArrowType;
import pl.wojciechkarpiel.ast.type.BaseType;
import pl.wojciechkarpiel.ast.util.Id;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class BetaEtaNormalTest {

    private Constant freshConstant() {
        return new Constant(Id.uniqueId(), new BaseType(Id.uniqueId()));
    }

    private Variable freshVariable() {
        return new Variable(Id.uniqueId(), new BaseType(Id.uniqueId()));
    }

    @Test
    void normalizeConstant() {
        Constant c = freshConstant();
        BetaEtaNormal ben = BetaEtaNormal.normalize(c);
        assertEquals(c, ben.getHead().getTerm());
        assertTrue(ben.isRigid());
        assertEquals(ben.getArguments(), new ArrayList<>());
        assertEquals(ben.getBinder(), new ArrayList<>());
    }

    @Test
    void normalizeFreeVar() {
        Variable c = new Variable(Id.uniqueId(), new BaseType(Id.uniqueId()));
        BetaEtaNormal ben = BetaEtaNormal.normalize(c);
        assertEquals(c, ben.getHead().getTerm());
        assertFalse(ben.isRigid());
        assertEquals(ben.getArguments(), new ArrayList<>());
        assertEquals(ben.getBinder(), new ArrayList<>());
    }

    @Test
    void normalizeApplication() {
        BaseType c2t = new BaseType(Id.uniqueId());
        Constant c2 = new Constant(Id.uniqueId(), c2t);
        Constant c1 = new Constant(Id.uniqueId(), new ArrowType(c2t, new BaseType(Id.uniqueId())));

        Application app = new Application(c1, c2);
        BetaEtaNormal ben = BetaEtaNormal.normalize(app);

        assertEquals(c1, ben.getHead().getTerm());
        assertEquals(new ArrayList<>(), ben.getBinder());
        ArrayList<Term> args = new ArrayList<>();
        args.add(c2);
        assertEquals(args, ben.getArguments());
    }

    @Test
    void normalizeAbstraction() {
        Constant c1 = freshConstant();
        Variable v = freshVariable();
        Abstraction app = new Abstraction(v, c1);
        BetaEtaNormal ben = BetaEtaNormal.normalize(app);

        assertTrue(ben.isRigid());
        assertEquals(c1, ben.getHead().getTerm());
        ArrayList<Variable> binders = new ArrayList<>();
        binders.add(v);
        assertEquals(binders, ben.getBinder());
        ArrayList<Term> args = new ArrayList<>();
        assertEquals(args, ben.getArguments());
    }

    @Test
    void normalizeLam_x_x() {
        Variable v = freshVariable();
        Abstraction app = new Abstraction(v, v);
        BetaEtaNormal ben = BetaEtaNormal.normalize(app);

        assertTrue(ben.isRigid());
        assertEquals(v, ben.getHead().getTerm());
        ArrayList<Variable> binders = new ArrayList<>();
        binders.add(v);
        assertEquals(binders, ben.getBinder());
        ArrayList<Term> args = new ArrayList<>();
        assertEquals(args, ben.getArguments());
    }

    @Test
    void checkBindersAndArgsOrder() {
        BaseType c2t = new BaseType(Id.uniqueId());
        Constant c2 = new Constant(Id.uniqueId(), c2t);
        BaseType c1t = new BaseType(Id.uniqueId());
        Constant c1 = new Constant(Id.uniqueId(), c1t);
        Variable v1 = new Variable(Id.uniqueId(), new BaseType(Id.uniqueId()));
        Variable v2 = freshVariable();
        Variable v = new Variable(Id.uniqueId(), new ArrowType(c1t, new ArrowType(c2t, new BaseType(Id.uniqueId()))));
        Term t = new Abstraction(v1, new Abstraction(v2, new Application(new Application(v, c1), c2)));
        BetaEtaNormal ben = BetaEtaNormal.normalize(t);

        assertFalse(ben.isRigid());
        assertEquals(v, ben.getHead().getTerm());
        ArrayList<Variable> binders = new ArrayList<>();
        binders.add(v1);
        binders.add(v2);
        assertEquals(binders, ben.getBinder());
        ArrayList<Term> args = new ArrayList<>();
        args.add(c1);
        args.add(c2);
        assertEquals(args, ben.getArguments());
    }

}