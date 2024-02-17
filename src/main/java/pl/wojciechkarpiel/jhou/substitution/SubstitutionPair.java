package pl.wojciechkarpiel.jhou.substitution;

import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.ast.Variable;
import pl.wojciechkarpiel.jhou.types.TypeCalculator;

import java.util.Objects;

public class SubstitutionPair {

    private final Variable variable;
    private final Term term;

    public SubstitutionPair(Variable variable, Term term) {
        this.variable = variable;
        this.term = term;
        TypeCalculator.ensureEqualTypes(variable, term);
    }

    public Variable getVariable() {
        return variable;
    }

    public Term getTerm() {
        return term;
    }

    @Override
    public String toString() {
        return "{" + variable + " → " + term + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubstitutionPair that = (SubstitutionPair) o;
        return Objects.equals(variable, that.variable) && Objects.equals(term, that.term);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variable, term);
    }
}
