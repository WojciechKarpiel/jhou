package pl.wojciechkarpiel.termHead;

import org.junit.jupiter.api.Test;
import pl.wojciechkarpiel.ast.Abstraction;
import pl.wojciechkarpiel.ast.Constant;
import pl.wojciechkarpiel.ast.Term;
import pl.wojciechkarpiel.ast.Variable;
import pl.wojciechkarpiel.ast.type.BaseType;
import pl.wojciechkarpiel.ast.util.Id;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HeaderUnifierTest {


    private Term freshLamxx() {
        Variable x = new Variable(Id.uniqueId(), new BaseType(Id.uniqueId()));
        return new Abstraction(x, x);
    }

    @Test
    void lamxxunif() {
        BetaEtaNormal a = BetaEtaNormalizer.normalize(freshLamxx());
        BetaEtaNormal b = BetaEtaNormalizer.normalize(freshLamxx());

        assertNotEquals(a, b);

        Optional<BetaEtaNormal> co = HeaderUnifier.alphaUnifyHeaderReturnNewRight(a, b);
        assertTrue(co.isPresent());
        BetaEtaNormal c = co.get();
        assertEquals(a, c);
    }

    @Test
    void noGood() {
        BetaEtaNormal a = BetaEtaNormalizer.normalize(freshLamxx());
        Variable v = new Variable(Id.uniqueId(), new BaseType(Id.uniqueId()));
        Constant c = new Constant(Id.uniqueId(), new BaseType(Id.uniqueId()));
        BetaEtaNormal b = BetaEtaNormalizer.normalize(new Abstraction(v, c));

        assertNotEquals(a, b);

        Optional<BetaEtaNormal> co = HeaderUnifier.alphaUnifyHeaderReturnNewRight(a, b);
        assertFalse(co.isPresent());
    }
}