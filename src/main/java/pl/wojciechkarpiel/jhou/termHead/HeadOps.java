package pl.wojciechkarpiel.jhou.termHead;

import pl.wojciechkarpiel.jhou.ast.Constant;
import pl.wojciechkarpiel.jhou.ast.Variable;

import java.util.Optional;

public class HeadOps {
    private HeadOps() {
    }


    public static class HeadIsNotAVariableException extends RuntimeException {
        private HeadIsNotAVariableException(Head head) {
            super(head + " is not a variable");
        }
    }

    public static Variable asVariableYolo(Head h) {
        return asVariable(h).orElseThrow(() -> new HeadIsNotAVariableException(h));
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
