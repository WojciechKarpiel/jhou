package pl.wojciechkarpiel.jhou.types;

import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.ast.type.ArrowType;
import pl.wojciechkarpiel.jhou.ast.type.Type;

public class WrongFunctionArgumentException extends TypeCheckException {
    private final Term function;
    private final ArrowType functionType;
    private final Term argument;
    private final Type argumentType;

    public WrongFunctionArgumentException(Term function, ArrowType functionType, Term argument, Type argumentType) {
        super("Attempted to call function " + function + " (type: " + functionType + ") " +
                "with an argument " + argument + " (type: " + argumentType + ")");
        this.function = function;
        this.functionType = functionType;
        this.argument = argument;
        this.argumentType = argumentType;
    }

    public Term getFunction() {
        return function;
    }

    public ArrowType getFunctionType() {
        return functionType;
    }

    public Term getArgument() {
        return argument;
    }

    public Type getArgumentType() {
        return argumentType;
    }
}
