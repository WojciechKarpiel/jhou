package pl.wojciechkarpiel.ast;

import pl.wojciechkarpiel.ast.util.Visitor;

public interface Term {

    <T> T visit(Visitor<T> visitor);
}
