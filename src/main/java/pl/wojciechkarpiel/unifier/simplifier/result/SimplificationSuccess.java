package pl.wojciechkarpiel.unifier.simplifier.result;

import pl.wojciechkarpiel.substitution.Substitution;

public class SimplificationSuccess implements SimplificationResult {

    private final Substitution solution;

    public SimplificationSuccess(Substitution solution) {
        this.solution = solution;
    }

    public Substitution getSolution() {
        return solution;
    }

    @Override
    public boolean isFailure() {
        return false;
    }
}
