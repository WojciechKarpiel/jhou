package pl.wojciechkarpiel.jhou.ast.type;

public interface Type {

    int arity();

    <T> T visit(TypeVisitor<T> visitor);

}