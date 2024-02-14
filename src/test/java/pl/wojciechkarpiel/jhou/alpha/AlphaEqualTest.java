package pl.wojciechkarpiel.jhou.alpha;

import org.junit.jupiter.api.Test;
import pl.wojciechkarpiel.jhou.ast.Abstraction;
import pl.wojciechkarpiel.jhou.ast.Variable;
import pl.wojciechkarpiel.jhou.ast.type.BaseType;
import pl.wojciechkarpiel.jhou.ast.type.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AlphaEqualTest {

    @Test
    void alphaEqual() {
        Type t = BaseType.freshBaseType();
        Variable x = Variable.freshVariable(t, "x");
        Variable y = Variable.freshVariable(t, "y");
        assertEquals(new Abstraction(x, x), new Abstraction(y, y));
        assertNotEquals(new Abstraction(x, y), new Abstraction(x, x));
        assertNotEquals(new Abstraction(y, x), new Abstraction(x, x));
        assertNotEquals(new Abstraction(y, x), new Abstraction(x, y));
    }
}