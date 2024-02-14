package pl.wojciechkarpiel.jhou.ast;

import pl.wojciechkarpiel.jhou.ast.util.Visitor;

public interface Term {

    <T> T visit(Visitor<T> visitor);
}
