package pl.wojciechkarpiel.jhou.termHead;

import org.junit.jupiter.api.Test;
import pl.wojciechkarpiel.jhou.ast.Abstraction;
import pl.wojciechkarpiel.jhou.ast.Constant;
import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.ast.Variable;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.normalizer.Normalizer;
import pl.wojciechkarpiel.jhou.types.TypeCalculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pl.wojciechkarpiel.jhou.Api.*;

class BetaEtaNormalizerTest {

    @Test
    void normalizevardouble() {
        Type t = freshType();
        Type tt = arrow(t, t);
        Variable v = freshVariable(tt, "u");
        BetaEtaNormal ben = BetaEtaNormal.normalize(v);
        assertEquals(1, ben.getBinder().size());
        assertEquals(1, ben.getArguments().size());
        assertEquals(v, ben.getHead().getTerm());
    }

    @Test
    void normalizevartripe() {
        Type t = freshType();
        Type y = freshType();
        Type z = freshType();
        Type tt = arrow(y, z);
        Type ttt = arrow(t, tt);
        Variable vt = freshVariable(ttt, "y");
        BetaEtaNormal bent = BetaEtaNormal.normalize(vt);
        assertEquals(2, bent.getBinder().size());
        assertEquals(t, bent.getBinder().get(0).getType());
        assertEquals(y, bent.getBinder().get(1).getType());
        assertEquals(2, bent.getArguments().size());
        assertEquals(t, TypeCalculator.calculateType(bent.getArguments().get(0)));
        assertEquals(y, TypeCalculator.calculateType(bent.getArguments().get(1)));
        assertEquals(vt, bent.getHead().getTerm());

        Term back = bent.backToTerm();
        assertEquals(typeOf(vt), typeOf(back));
        assertEquals(vt, Normalizer.etaContract(back));
    }

    @Test
    void normalizeLam() {
        Type t = freshType();
        Abstraction lamxx = (Abstraction) abstraction(t, x -> x);
        BetaEtaNormal l = BetaEtaNormal.normalize(lamxx);
        assertEquals(1, l.getBinder().size());
        assertEquals(0, l.getArguments().size());
        assertEquals(l.getHead().getTerm(), l.getBinder().get(0));
    }


    @Test
    void nornor() {
        Type a = freshType("a");
        Type b = freshType("b");
        Type c = freshType("c");
        Type d = freshType("d");
        Type abcd = arrow(a, arrow(b, arrow(c, d)));
        Variable v = freshVariable(abcd, "v");

        Type q = freshType("q");
        Constant A = Constant.freshConstant(a, "A");
        Term t = abstraction(q, "q", x -> app(v, A));
        typeOf(t);
        BetaEtaNormal ben = BetaEtaNormalizer.normalize(t);

        assertEquals(t, etaContract(ben.backToTerm()));
        assertEquals(3, ben.getBinder().size());
        assertEquals(q, ben.getBinder().get(0).getType());
        assertEquals(b, ben.getBinder().get(1).getType());
        assertEquals(c, ben.getBinder().get(2).getType());
        assertEquals(3, ben.getArguments().size());
        assertEquals(a, typeOf(ben.getArguments().get(0)));
        assertEquals(b, typeOf(ben.getArguments().get(1)));
        assertEquals(c, typeOf(ben.getArguments().get(2)));
        assertEquals(v, ben.getHead().getTerm());

    }
}