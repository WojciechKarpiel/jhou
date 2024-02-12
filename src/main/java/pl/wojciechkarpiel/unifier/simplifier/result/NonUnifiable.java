package pl.wojciechkarpiel.unifier.simplifier.result;

public enum NonUnifiable implements SimplificationResult {
    INSTANCE;

    @Override
    public boolean isFailure() {
        return true;
    }

    @Override
    public <T> T visit(SimplificationVisitor<T> visitor) {
        return visitor.visitFailure(this);
    }
}
