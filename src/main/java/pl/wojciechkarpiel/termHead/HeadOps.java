package pl.wojciechkarpiel.termHead;

import pl.wojciechkarpiel.ast.Constant;
import pl.wojciechkarpiel.ast.Variable;

import java.util.Optional;

public class HeadOps {
    private HeadOps() {
    }


    public static Optional<Variable> asVariable(Head h) {
        return h.visit(new Head.HeadVisitor<Optional<Variable>>() {
            @Override
            public Optional<Variable> visitConstant(Constant constant) {
                return Optional.empty();
            }

            @Override
            public Optional<Variable> visitVariable(Variable variable) {
                return Optional.of(variable);
            }
        });
    }


    public static Optional<Constant> asConstant(Head h) {
        return h.visit(new Head.HeadVisitor<Optional<Constant>>() {
            @Override
            public Optional<Constant> visitConstant(Constant constant) {
                return Optional.of(constant);
            }

            @Override
            public Optional<Constant> visitVariable(Variable variable) {
                return Optional.empty();
            }
        });
    }
}
