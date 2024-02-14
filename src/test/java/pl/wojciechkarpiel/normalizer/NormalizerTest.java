package pl.wojciechkarpiel.normalizer;

import org.junit.jupiter.api.Test;
import pl.wojciechkarpiel.ast.*;
import pl.wojciechkarpiel.ast.type.ArrowType;
import pl.wojciechkarpiel.ast.type.BaseType;
import pl.wojciechkarpiel.ast.type.Type;
import pl.wojciechkarpiel.ast.util.Id;

import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO test variable shadowing
class NormalizerTest {
    @org.junit.jupiter.api.Test
    void normalizeConstant() {
        Constant c = new Constant(Id.uniqueId(), new BaseType(Id.uniqueId()));
        assertEquals(c, Normalizer.betaNormalize(c));
    }

    @org.junit.jupiter.api.Test
    void normalizeFreeVariable() {
        Variable v = new Variable(Id.uniqueId(), new BaseType(Id.uniqueId()));
        assertEquals(v, Normalizer.betaNormalize(v));
    }

    @org.junit.jupiter.api.Test
    void normalizeApplication() {
        Type type = new BaseType(Id.uniqueId());
        Variable v = new Variable(Id.uniqueId(), type);
        Abstraction abs = new Abstraction(v, v);
        Constant c = new Constant(Id.uniqueId(), type);
        Application app = new Application(abs, c);
        assertEquals(c, Normalizer.betaNormalize(app));
    }

    @Test
    void etaNormalization() {
        Type t = BaseType.freshBaseType();
        Type y = BaseType.freshBaseType();
        Constant f = Constant.freshConstant(new ArrowType(t, y), "F");
        Variable x = Variable.freshVariable(t, "x");

        Term abs = new Abstraction(x, new Application(f, x));
        assertEquals(f, Normalizer.etaNormalize(abs));
    }
}