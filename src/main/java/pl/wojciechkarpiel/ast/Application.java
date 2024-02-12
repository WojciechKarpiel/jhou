package pl.wojciechkarpiel.ast;

import pl.wojciechkarpiel.ast.util.Visitor;

import java.util.Objects;

public class Application implements Term {

    private final Term function;
    private final Term argument;

    public Application(Term function, Term argument) {
        this.function = function;
        this.argument = argument;
    }

    public Term getArgument() {
        return argument;
    }

    public Term getFunction() {
        return function;
    }

    @Override
    public String toString() {
        return "Application{" +
                "function=" + function +
                ", argument=" + argument +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Application that = (Application) o;
        return Objects.equals(function, that.function) && Objects.equals(argument, that.argument);
    }

    @Override
    public int hashCode() {
        return Objects.hash(function, argument);
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
        return visitor.visitApplication(this);
    }
}
