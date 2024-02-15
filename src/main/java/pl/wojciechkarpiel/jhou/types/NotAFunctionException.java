package pl.wojciechkarpiel.jhou.types;

import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.ast.type.Type;

public class NotAFunctionException extends TypeCheckException {

    private final Term supposedToBeFunctionTerm;
    private final Type actualType;

    public NotAFunctionException(Term supposedToBeFunctionTerm, Type actualType) {
        super("Term " + supposedToBeFunctionTerm + " was expected to be a function type, " +
                "but has type " + actualType + " instead");
        this.supposedToBeFunctionTerm = supposedToBeFunctionTerm;
        this.actualType = actualType;
    }

    public Term getSupposedToBeFunctionTerm() {
        return supposedToBeFunctionTerm;
    }

    public Type getActualType() {
        return actualType;
    }
}
