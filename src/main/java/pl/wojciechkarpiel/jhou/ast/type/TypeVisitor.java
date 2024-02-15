package pl.wojciechkarpiel.jhou.ast.type;

public interface TypeVisitor<T> {
    T visitBaseType(BaseType baseType);

    T visitArrowType(ArrowType arrowType);
}
