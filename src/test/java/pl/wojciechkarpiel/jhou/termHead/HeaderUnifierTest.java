package pl.wojciechkarpiel.jhou.termHead;

import org.junit.jupiter.api.Test;
import pl.wojciechkarpiel.jhou.Api;
import pl.wojciechkarpiel.jhou.ast.Abstraction;
import pl.wojciechkarpiel.jhou.ast.Constant;
import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.ast.Variable;
import pl.wojciechkarpiel.jhou.ast.type.BaseType;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.ast.util.Id;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HeaderUnifierTest {


    private Term freshLamxx(Type t) {
        Variable x = new Variable(Id.uniqueId(), t);
        return new Abstraction(x, x);
    }

    @Test
    void lamxxunif() {
        Type t = Api.freshType();
        BetaEtaNormal a = BetaEtaNormalizer.normalize(freshLamxx(t));
        BetaEtaNormal b = BetaEtaNormalizer.normalize(freshLamxx(t));

        assertNotEquals(a, b);

        Optional<BetaEtaNormal> co = HeaderUnifier.alphaUnifyHeaderReturnNewRight(a, b);
        assertTrue(co.isPresent());
        BetaEtaNormal c = co.get();
        assertEquals(a, c);
    }

    @Test
    void noGood() {
        Type t = Api.freshType("T");
        BetaEtaNormal a = BetaEtaNormalizer.normalize(freshLamxx(t));
        Variable v = new Variable(Id.uniqueId(), t);
        Constant c = new Constant(Id.uniqueId(), t);
        BetaEtaNormal b = BetaEtaNormalizer.normalize(new Abstraction(v, c));

        assertNotEquals(a, b);

        Optional<BetaEtaNormal> co = HeaderUnifier.alphaUnifyHeaderReturnNewRight(a, b);
        assertFalse(co.isPresent());
    }

    @Test
    void ididunifiable() {
        Type xt = BaseType.freshBaseType();
        Variable x = Variable.freshVariable(xt, "x");
        Term lamxx = new Abstraction(x, x);
        Variable y = Variable.freshVariable(xt, "y");
        Term lamyy = new Abstraction(y, y);
        assertTrue(HeaderUnifier.headAlphaUnifiable(lamxx, lamyy));
    }

    @Test
    void failNonUnifiable() {
        Type xt = BaseType.freshBaseType();
        Constant x = Constant.freshConstant(xt, "x");
        Constant y = Constant.freshConstant(xt, "y");
        assertFalse(HeaderUnifier.headAlphaUnifiable(x, y));
    }

    @Test
    void vauouslyUnif() {
        Type xt = BaseType.freshBaseType();
        Constant x = Constant.freshConstant(xt, "x");
        Constant y = new Constant(x.getId(), x.getType());
        assertTrue(HeaderUnifier.headAlphaUnifiable(x, y));
    }
}