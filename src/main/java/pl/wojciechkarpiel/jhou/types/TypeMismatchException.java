package pl.wojciechkarpiel.jhou.types;

import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.ast.type.Type;

public class TypeMismatchException extends TypeCheckException {

    private final Term a;
    private final Type aType;
    private final Term b;
    private final Type bType;

    public TypeMismatchException(Term a, Type aType, Term b, Type bType) {
        super("Expected types of " + a + " (type: " + aType + ") " +
                "and of " + b + " (type: " + bType + ") to be equal.");
        this.a = a;
        this.aType = aType;
        this.b = b;
        this.bType = bType;
    }

    public Type getbType() {
        return bType;
    }

    public Term getB() {
        return b;
    }

    public Type getaType() {
        return aType;
    }

    public Term getA() {
        return a;
    }
}
