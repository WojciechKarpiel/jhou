package pl.wojciechkarpiel.jhou.ast.type;

import java.util.Objects;

public class ArrowType implements Type {

    private final Type from;
    private final Type to;

    public ArrowType(Type from, Type to) {
        this.from = from;
        this.to = to;
    }

    public Type getFrom() {
        return from;
    }

    public Type getTo() {
        return to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrowType arrowType = (ArrowType) o;
        return Objects.equals(from, arrowType.from) && Objects.equals(to, arrowType.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public String toString() {
        return "Arrow(" + from + "->" + to + ")";
    }

    @Override
    public int arity() {
        return 1 + to.arity();
    }
}
