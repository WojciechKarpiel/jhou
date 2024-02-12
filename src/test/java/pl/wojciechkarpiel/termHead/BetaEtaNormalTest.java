package pl.wojciechkarpiel.termHead;

import org.junit.jupiter.api.Test;
import pl.wojciechkarpiel.ast.*;
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
        Constant c1 = freshConstant();
        Constant c2 = freshConstant();
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


        assertFalse(ben.isRigid());
        assertEquals(v, ben.getHead().getTerm());
        ArrayList<Variable> binders = new ArrayList<>();
        binders.add(v);
        assertEquals(binders, ben.getBinder());
        ArrayList<Term> args = new ArrayList<>();
        assertEquals(args, ben.getArguments());
    }
}