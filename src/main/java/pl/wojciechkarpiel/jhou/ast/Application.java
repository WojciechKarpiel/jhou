package pl.wojciechkarpiel.jhou.ast;

import pl.wojciechkarpiel.jhou.ast.util.Visitor;

import java.util.Objects;

public class Application implements Term {

    private final Term function;
    private final Term argument;

    public Application(Term function, Term argument) {
        this.function = function;
        this.argument = argument;
    }

    public static Application apply(Term function, Term argument, Term... moreArguments) {
        Application result = new Application(function, argument);
        for (Term additionalArgument : moreArguments) {
            result = new Application(result, additionalArgument);
        }
        return result;
    }

    public Term getArgument() {
        return argument;
    }

    public Term getFunction() {
        return function;
    }

    @Override
    public String toString() {
        return "(" + function + " " + argument + ")";
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
