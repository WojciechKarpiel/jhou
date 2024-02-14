package pl.wojciechkarpiel.jhou.unifier.simplifier.result;

public interface SimplificationResult {

    boolean isFailure();

    <T> T visit(SimplificationVisitor<T> visitor);
}
