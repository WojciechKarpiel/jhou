package pl.wojciechkarpiel.ast;

import pl.wojciechkarpiel.ast.util.Visitor;

import java.util.Objects;

public class Abstraction implements Term {

    private final Variable variable;
    private final Term body;

    public Abstraction(Variable variable, Term body) {
        this.variable = variable;
        this.body = body;
    }

    public Variable getVariable() {
        return variable;
    }


    public Term getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "(fn[" + variable + "]" + body + ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Abstraction that = (Abstraction) o;
        return Objects.equals(variable, that.variable) && Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variable, body);
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
        return visitor.visitAbstraction(this);
    }
}
