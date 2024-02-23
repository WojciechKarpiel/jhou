package pl.wojciechkarpiel.jhou.unifier.simplifier;

import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.ast.Variable;
import pl.wojciechkarpiel.jhou.substitution.Substitution;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class PossibleSubstitutions {

    private final Variable variable;
    private final List<Term> substitutions;

    public PossibleSubstitutions(Variable variable, List<Term> substitutions) {
        this.variable = variable;
        this.substitutions = substitutions;
    }

    public Stream<Substitution> intoSubstitutionStream() {
        return getSubstitutions().stream().map(term -> new Substitution(getVariable(), term));
    }

    public List<Term> getSubstitutions() {
        return substitutions;
    }

    public Variable getVariable() {
        return variable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PossibleSubstitutions that = (PossibleSubstitutions) o;
        return Objects.equals(variable, that.variable) && Objects.equals(substitutions, that.substitutions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variable, substitutions);
    }
}
