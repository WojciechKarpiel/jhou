package pl.wojciechkarpiel.jhou.ast;

import pl.wojciechkarpiel.jhou.ast.util.Visitor;

/**
 * term.equals is raw structural equality. λx.x and λy.y are not equal.
 * For weaker notions of equality (alpha/beta/eta), see the Api.
 */
public interface Term {
    <T> T visit(Visitor<T> visitor);
}
