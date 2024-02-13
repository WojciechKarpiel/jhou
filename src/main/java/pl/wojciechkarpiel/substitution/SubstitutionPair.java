package pl.wojciechkarpiel.substitution;

import pl.wojciechkarpiel.ast.Term;
import pl.wojciechkarpiel.ast.Variable;

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
        return "SubstitutionPair{" + variable + ", " + term + '}';
    }
}
