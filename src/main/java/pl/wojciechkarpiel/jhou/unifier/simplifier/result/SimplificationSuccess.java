package pl.wojciechkarpiel.jhou.unifier.simplifier.result;

import pl.wojciechkarpiel.jhou.substitution.Substitution;

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

    @Override
    public <T> T visit(SimplificationVisitor<T> visitor) {
        return visitor.visitSuccess(this);
    }
}
