package pl.wojciechkarpiel.normalizer;

import pl.wojciechkarpiel.ast.Abstraction;
import pl.wojciechkarpiel.ast.Application;
import pl.wojciechkarpiel.ast.Constant;
import pl.wojciechkarpiel.ast.Variable;
import pl.wojciechkarpiel.ast.type.BaseType;
import pl.wojciechkarpiel.ast.type.Type;
import pl.wojciechkarpiel.ast.util.Id;

import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO test variable shadowing
class NormalizerTest {
    @org.junit.jupiter.api.Test
    void normalizeConstant() {
        Constant c = new Constant(Id.uniqueId(), new BaseType(Id.uniqueId()));
        assertEquals(c, Normalizer.normalize(c));
    }

    @org.junit.jupiter.api.Test
    void normalizeFreeVariable() {
        Variable v = new Variable(Id.uniqueId(), new BaseType(Id.uniqueId()));
        assertEquals(v, Normalizer.normalize(v));
    }

    @org.junit.jupiter.api.Test
    void normalizeApplication() {
        Type type = new BaseType(Id.uniqueId());
        Variable v = new Variable(Id.uniqueId(), type);
        Abstraction abs = new Abstraction(v, v);
        Constant c = new Constant(Id.uniqueId(), type);
        Application app = new Application(abs, c);
        assertEquals(c, Normalizer.normalize(app));
    }
}