package pl.wojciechkarpiel.substitution;

import pl.wojciechkarpiel.ast.Term;
import pl.wojciechkarpiel.ast.Variable;

import java.util.Map;

public class Substitution {

    private final Map<Variable, Term> substitution;

    public Substitution(Map<Variable, Term> substitution) {
        this.substitution = substitution;
    }
}
