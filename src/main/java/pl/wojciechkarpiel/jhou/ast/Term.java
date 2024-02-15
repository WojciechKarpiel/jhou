package pl.wojciechkarpiel.jhou.ast;

import pl.wojciechkarpiel.jhou.ast.util.Visitor;

/**
 * term.equals is equality modulo alpha conversion. It does not take into account beta nor eta conversions
 */
public interface Term {
    <T> T visit(Visitor<T> visitor);
}
