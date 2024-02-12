package pl.wojciechkarpiel.unifier.simplifier.result;

import java.util.Optional;

public class SimplificationOps {
    private SimplificationOps() {
    }

    public static Optional<SimplificationNode> asNode(SimplificationResult r) {
        return r.visit(new SimplificationVisitor<Optional<SimplificationNode>>() {
            @Override
            public Optional<SimplificationNode> visitSuccess(SimplificationSuccess success) {
                return Optional.empty();
            }

            @Override
            public Optional<SimplificationNode> visitNode(SimplificationNode node) {
                return Optional.of(node);
            }

            @Override
            public Optional<SimplificationNode> visitFailure(NonUnifiable nonUnifiable) {
                return Optional.empty();
            }
        });
    }

    public static Optional<NonUnifiable> asFailure(SimplificationResult r) {
        return r.visit(new SimplificationVisitor<Optional<NonUnifiable>>() {
            @Override
            public Optional<NonUnifiable> visitSuccess(SimplificationSuccess success) {
                return Optional.empty();
            }

            @Override
            public Optional<NonUnifiable> visitNode(SimplificationNode node) {
                return Optional.empty();
            }

            @Override
            public Optional<NonUnifiable> visitFailure(NonUnifiable nonUnifiable) {
                return Optional.of(nonUnifiable);
            }
        });
    }

    public static Optional<SimplificationSuccess> asSuccess(SimplificationResult r) {
        return r.visit(new SimplificationVisitor<Optional<SimplificationSuccess>>() {
            @Override
            public Optional<SimplificationSuccess> visitSuccess(SimplificationSuccess success) {
                return Optional.of(success);
            }

            @Override
            public Optional<SimplificationSuccess> visitNode(SimplificationNode node) {
                return Optional.empty();
            }

            @Override
            public Optional<SimplificationSuccess> visitFailure(NonUnifiable nonUnifiable) {
                return Optional.empty();
            }
        });
    }
}
