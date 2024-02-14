package pl.wojciechkarpiel.jhou.termHead;

import org.junit.jupiter.api.Test;
import pl.wojciechkarpiel.jhou.ast.Abstraction;
import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.ast.Variable;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.normalizer.Normalizer;
import pl.wojciechkarpiel.jhou.types.TypeCalculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pl.wojciechkarpiel.jhou.api.Api.*;

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
        assertEquals(vt, Normalizer.etaNormalize(back));
        System.out.println(back);
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
}