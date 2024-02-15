package pl.wojciechkarpiel.jhou.types;

public abstract class TypeCheckException extends RuntimeException {

    public TypeCheckException(String msg) {
        super(msg);
    }
}
