package pl.wojciechkarpiel.ast;

import pl.wojciechkarpiel.ast.type.Type;
import pl.wojciechkarpiel.ast.util.Id;
import pl.wojciechkarpiel.ast.util.Visitor;

import java.util.Objects;

public class Variable implements Term {

    private final Id id;
    private final Type type;

    public Variable(Id id, Type type) {
        this.id = id;
        this.type = type;
    }

    public Id getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Variable{" +
                "id=" + id +
                ", type=" + type +
                '}';
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
