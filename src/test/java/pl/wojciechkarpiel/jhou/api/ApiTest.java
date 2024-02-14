package pl.wojciechkarpiel.jhou.api;

import org.junit.jupiter.api.Test;
import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.ast.Variable;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.substitution.Substitution;
import pl.wojciechkarpiel.jhou.unifier.SolutionIterator;

import static org.junit.jupiter.api.Assertions.*;
import static pl.wojciechkarpiel.jhou.api.Api.*;

class ApiTest {

    /**
     * Example of unifying `λx.y (C (y x))` and `λx.C x`.
     * The result is `y → λx.x`
     */
    @Test
    void apiUsageExample() {
        // GIVEN
        Type type = freshType(); // we work with typed lambda calculus, so we need some type
        Term c = freshConstant(arrowType(type, type), "C");
        Variable y = freshVariable(arrowType(type, type), "y");
        Term left = abstraction(type, x -> app(y, app(c, app(y, x))));
        Term right = abstraction(type, x -> app(c, x));

        // WHEN
        // result is an iterator over possible substitutions that unify the two sider
        SolutionIterator result = unify(left, right);

        // THEN
        assertTrue(result.hasNext());
        Substitution solution = result.next();
        assertFalse(result.hasNext()); // only one solution in this case

        // check if shape of substitution is the one we expect
        Substitution expectedSolution = new Substitution(y, abstraction(type, x -> x));
        assertEquals(expectedSolution, solution);

        // let's also do the substitutions for the final check
        assertNotEquals(left, right);
        assertEquals(
                betaNormalize(solution.substitute(left)),
                betaNormalize(solution.substitute(right))
        );
    }
}