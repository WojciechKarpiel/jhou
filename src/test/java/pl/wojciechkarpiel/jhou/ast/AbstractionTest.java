package pl.wojciechkarpiel.jhou.ast;

import org.junit.jupiter.api.Test;
import pl.wojciechkarpiel.jhou.ast.type.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pl.wojciechkarpiel.jhou.Api.abstraction;
import static pl.wojciechkarpiel.jhou.Api.freshType;

class AbstractionTest {

    @Test
    void equalsHashcodeContractTest() {
        Type t = freshType();
        Term ax = abstraction(t, x -> x);
        Term ay = abstraction(t, x -> x);
        assertHashcodeEquals(ax, ay);
        assertHashcodeEquals(ax, ax);
        assertHashcodeEquals(ay, ay);
    }


    private void assertHashcodeEquals(Object a, Object b) {
        if (a.equals(b)) {
            assertEquals(a.hashCode(), b.hashCode());
        }
    }
}