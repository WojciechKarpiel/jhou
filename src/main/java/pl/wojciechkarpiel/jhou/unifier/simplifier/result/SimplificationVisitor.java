package pl.wojciechkarpiel.jhou.unifier.simplifier.result;

public interface SimplificationVisitor<T> {
    T visitSuccess(SimplificationSuccess success);

    T visitNode(SimplificationNode node);

    T visitFailure(NonUnifiable nonUnifiable);
}
