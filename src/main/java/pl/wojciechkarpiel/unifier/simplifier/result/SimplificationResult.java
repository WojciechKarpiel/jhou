package pl.wojciechkarpiel.unifier.simplifier.result;

public interface SimplificationResult {

    boolean isFailure();

    <T> T visit(SimplificationVisitor<T> visitor);
}
