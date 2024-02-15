package pl.wojciechkarpiel.jhou.termHead;

import org.junit.jupiter.api.Test;
import pl.wojciechkarpiel.jhou.Api;
import pl.wojciechkarpiel.jhou.ast.*;
import pl.wojciechkarpiel.jhou.ast.type.ArrowType;
import pl.wojciechkarpiel.jhou.ast.type.BaseType;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.ast.util.Id;
import pl.wojciechkarpiel.jhou.util.ListUtil;

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
        assertEquals(c, ben.backToTerm());
    }

    @Test
    void normalizeFreeVar() {
        Variable c = new Variable(Id.uniqueId(), new BaseType(Id.uniqueId()));
        BetaEtaNormal ben = BetaEtaNormal.normalize(c);
        assertEquals(c, ben.getHead().getTerm());
        assertFalse(ben.isRigid());
        assertEquals(ben.getArguments(), new ArrayList<>());
        assertEquals(ben.getBinder(), new ArrayList<>());
        assertEquals(c, ben.backToTerm());
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
        assertEquals(app, ben.backToTerm());
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
        assertEquals(app, ben.backToTerm());
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
        assertEquals(app, ben.backToTerm());
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
        assertEquals(t, ben.backToTerm());

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

    @Test
    void brokenEta() {
        Type t = Api.freshType("T");
        Constant c = (Constant) Api.freshConstant(Api.arrow(t, t), "C");

        Head.HeadConstant head = new Head.HeadConstant(c);
        BetaEtaNormal fakeNormal =
                new BetaEtaNormal(head, ListUtil.of(), ListUtil.of());
        BetaEtaNormal realNormal =
                BetaEtaNormal.fromFakeNormal(head, ListUtil.of(), ListUtil.of());

        Term expanded = Api.etaExpand(c);
        assertEquals(c, fakeNormal.backToTerm());
        assertEquals(expanded, realNormal.backToTerm());
        assertNotEquals(realNormal.backToTerm(), fakeNormal.backToTerm());
        assertEquals(c, Api.etaContract(realNormal.backToTerm()));
        assertEquals(expanded, Api.etaExpand(fakeNormal.backToTerm()));
    }
}