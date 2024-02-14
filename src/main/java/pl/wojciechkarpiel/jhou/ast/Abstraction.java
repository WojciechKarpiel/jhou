package pl.wojciechkarpiel.jhou.ast;

import pl.wojciechkarpiel.jhou.alpha.AlphaEqual;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.ast.util.Visitor;

import java.util.Objects;
import java.util.function.Function;

public class Abstraction implements Term {

    private final Variable variable;
    private final Term body;

    public Abstraction(Variable variable, Term body) {
        this.variable = variable;
        this.body = body;
    }

    public static Abstraction fromLambda(Type variableType, Function<Variable, Term> abstraction) {
        Variable v = Variable.freshVariable(variableType);
        Term body = abstraction.apply(v);
        return new Abstraction(v, body);
    }

    public static Abstraction fromLambda(Type variableType, Function<Variable, Term> abstraction, String variableName) {
        Variable v = Variable.freshVariable(variableType, variableName);
        Term body = abstraction.apply(v);
        return new Abstraction(v, body);
    }

    public Variable getVariable() {
        return variable;
    }


    public Term getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "λ" + variable + "." + body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Abstraction that = (Abstraction) o;
        return AlphaEqual.isAlphaEqual(this, that);
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
