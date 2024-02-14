package pl.wojciechkarpiel.jhou.substitution;

import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.ast.Variable;

public class SubstitutionPair {

    private final Variable variable;
    private final Term term;

    public SubstitutionPair(Variable variable, Term term) {
        this.variable = variable;
        this.term = term;
    }

    public Variable getVariable() {
        return variable;
    }

    public Term getTerm() {
        return term;
    }

    @Override
    public String toString() {
        return "{" + variable + " -> " + term + '}';
    }
}
