package pl.wojciechkarpiel.jhou.ast;

import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.ast.util.Id;
import pl.wojciechkarpiel.jhou.ast.util.Visitor;

import java.util.Objects;

public class Variable implements Term {

    private final Id id;
    private final Type type;
    private final String name;

    public Variable(Id id, Type type) {
        this(id, type, null);
    }

    public Variable(Id id, Type type, String name) {
        this.id = id;
        this.type = type;
        this.name = name;
    }

    public static Variable freshVariable(Type type) {
        return new Variable(Id.uniqueId(), type);
    }

    public static Variable freshVariable(Type type, String name) {
        return new Variable(Id.uniqueId(), type, name);
    }

    public Id getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return name != null ? name : "V_" + id.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable = (Variable) o;
        return Objects.equals(id, variable.id) && Objects.equals(type, variable.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
        return visitor.visitVariable(this);
    }
}
