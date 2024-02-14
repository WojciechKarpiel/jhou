package pl.wojciechkarpiel.types;

import pl.wojciechkarpiel.ast.*;
import pl.wojciechkarpiel.ast.type.ArrowType;
import pl.wojciechkarpiel.ast.type.Type;
import pl.wojciechkarpiel.ast.util.Visitor;

public class TypeCalculator {

    public static Type calculateType(Term term) {
        return term.visit(new Visitor<Type>() {
            public Type visitConstant(Constant constant) {
                return constant.getType();
            }

            @Override
            public Type visitVariable(Variable variable) {
                return variable.getType();
            }

            @Override
            public Type visitApplication(Application application) {
                Term fn = application.getFunction();
                Term arg = application.getArgument();
                Type fnType = calculateType(fn);
                if (fnType instanceof ArrowType) {
                    ArrowType fnTa = (ArrowType) fnType;
                    Type argType = calculateType(arg);
                    if (fnTa.getFrom().equals(argType)) {
                        return fnTa.getTo();
                    } else throw new TypeMismatchException("Arg type expected and given wrong:");
                } else throw new TypeMismatchException(" not a fn");
            }

            @Override
            public Type visitAbstraction(Abstraction abstraction) {
                Type from = abstraction.getVariable().getType();
                Type to = calculateType(abstraction.getBody());
                return new ArrowType(from, to);
            }
        });
    }
}
