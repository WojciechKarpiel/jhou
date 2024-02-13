package pl.wojciechkarpiel.ast.util;

public class Id {
    private static int COUNTER = 0;
    private final int id;


    private Id(int id) {
        this.id = id;
    }

    public static Id uniqueId() {
        return new Id(COUNTER++);
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Id(" + id + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Id id1 = (Id) o;
        return id == id1.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
