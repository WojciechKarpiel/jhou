package pl.wojciechkarpiel.ast;

import pl.wojciechkarpiel.ast.type.Type;
import pl.wojciechkarpiel.ast.util.Id;
import pl.wojciechkarpiel.ast.util.Visitor;

import java.util.Objects;

public class Constant implements Term {

    private final Id id;
    private final Type type;
    private final String name;

    public Constant(Id id, Type type) {
        this(id, type, null);
    }

    /**
     * @param name For printing only, equality is decided by Id
     */
    public Constant(Id id, Type type, String name) {
        this.id = id;
        this.type = type;
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public Id getId() {
        return id;
    }

    @Override
    public String toString() {
        return name != null ? name : "C(" + id.getId() + ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Constant constant = (Constant) o;
        return Objects.equals(id, constant.id) && Objects.equals(type, constant.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
        return visitor.visitConstant(this);
    }
}
