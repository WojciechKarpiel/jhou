package pl.wojciechkarpiel.jhou.types;

import pl.wojciechkarpiel.jhou.ast.*;
import pl.wojciechkarpiel.jhou.ast.type.ArrowType;
import pl.wojciechkarpiel.jhou.ast.type.BaseType;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.ast.type.TypeVisitor;
import pl.wojciechkarpiel.jhou.ast.util.Visitor;

public class TypeCalculator {

    public static void ensureEqualTypes(Term a, Term b) {
        Type aType = calculateType(a);
        Type bType = calculateType(b);
        if (!aType.equals(bType)) {
            throw new TypeMismatchException(a, aType, b, bType);
        }
    }

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
                    } else throw new WrongFunctionArgumentException(fn, fnTa, arg, argType);
                } else throw new NotAFunctionException(fn, fnType);
            }

            @Override
            public Type visitAbstraction(Abstraction abstraction) {
                Type from = abstraction.getVariable().getType();
                Type to = calculateType(abstraction.getBody());
                return new ArrowType(from, to);
            }
        });
    }

    public static ArrowType ensureArrowType(Term term) {
        return calculateType(term).visit(new TypeVisitor<ArrowType>() {
            public ArrowType visitBaseType(BaseType baseType) {
                throw new NotAFunctionException(term, baseType);
            }

            public ArrowType visitArrowType(ArrowType arrowType) {
                return arrowType;
            }
        });
    }
}
