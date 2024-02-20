package pl.wojciechkarpiel.jhou.ast.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pl.wojciechkarpiel.jhou.Api.arrow;
import static pl.wojciechkarpiel.jhou.Api.freshType;

class ArrowTypeTest {

    @Test
    void typeOfCurriedFunction() {
        Type a = freshType();
        Type b = freshType();
        Type c = freshType();
        Type d = freshType();
        Type e = freshType();
        ArrowType result = ArrowType.typeOfCurriedFunction(a, b, c, d, e);
        assertEquals(arrow(a, arrow(b, arrow(c, arrow(d, e)))), result);
        assertEquals(arrow(c, d), ArrowType.typeOfCurriedFunction(c, d));
        assertEquals(arrow(c, arrow(arrow(a, d), e)), ArrowType.typeOfCurriedFunction(c, arrow(a, d), e));
    }
}