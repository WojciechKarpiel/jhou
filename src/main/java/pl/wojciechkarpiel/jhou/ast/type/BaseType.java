package pl.wojciechkarpiel.jhou.ast.type;

import pl.wojciechkarpiel.jhou.ast.util.Id;

import java.util.Objects;

public class BaseType implements Type {

    private final Id id;
    private final String name;

    public BaseType(Id id) {
        this(id, null);
    }

    public BaseType(Id id, String name) {
        this.id = id;
        this.name = name;
    }

    public static BaseType freshBaseType() {
        return new BaseType(Id.uniqueId());
    }

    public static BaseType freshBaseType(String name) {
        return new BaseType(Id.uniqueId(), name);
    }

    @Override
    public String toString() {
        return name != null ? name : "T_" + id.getId();
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
