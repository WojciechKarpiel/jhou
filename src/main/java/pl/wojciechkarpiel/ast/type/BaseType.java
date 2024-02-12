package pl.wojciechkarpiel.ast.type;

import pl.wojciechkarpiel.ast.util.Id;

import java.util.Objects;

public class BaseType implements Type {

    private final Id id;

    public BaseType(Id id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Type(" + id + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseType type = (BaseType) o;
        return Objects.equals(id, type.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int arity() {
        return 0;
    }
}
