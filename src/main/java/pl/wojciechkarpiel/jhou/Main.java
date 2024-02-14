package pl.wojciechkarpiel.jhou;

import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.ast.Variable;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.substitution.Substitution;
import pl.wojciechkarpiel.jhou.unifier.SolutionIterator;

import static pl.wojciechkarpiel.jhou.api.Api.*;

public class Main {

    /**
     * Example of unifying `λx.y (C (y x))` and `λx.C x`.
     * The result is `y → λx.x`
     */
    public static void main(String[] args) {
        Type type = freshType(); // we work with typed lambda calculus, so we need some type
        Term c = freshConstant(arrow(type, type), "C");
        Variable y = freshVariable(arrow(type, type), "y");
        Term left = abstraction(type, x -> app(y, app(c, app(y, x))));
        Term right = abstraction(type, x -> app(c, x));
        // result is an iterator over possible substitutions that unify the two sider
        SolutionIterator result = unify(left, right);
        Substitution solution = result.next();
        System.out.println(solution);
        // prints: Substitution{[{y → λV_7.V_7}]}
    }
}
