package pl.wojciechkarpiel.jhou.ast.type;

import java.util.*;

public class ArrowType implements Type {

    private final Type from;
    private final Type to;

    public ArrowType(Type from, Type to) {
        this.from = from;
        this.to = to;
    }

    public static ArrowType typeOfCurriedFunction(Type arg1, Type arg2, Type... argN) {
        List<Type> args = new ArrayList<>(argN.length + 2);
        args.add(arg1);
        args.add(arg2);
        args.addAll(Arrays.asList(argN));
        Collections.reverse(args);
        Type result = args.get(0);
        for (int i = 1; i < args.size(); i++) {
            result = new ArrowType(args.get(i), result);
        }
        return (ArrowType) result;
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
        return "(" + from + "→" + to + ")";
    }

    @Override
    public int arity() {
        return 1 + to.arity();
    }

    @Override
    public <T> T visit(TypeVisitor<T> visitor) {
        return visitor.visitArrowType(this);
    }
}
