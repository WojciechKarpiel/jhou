package pl.wojciechkarpiel.ast.util;

import pl.wojciechkarpiel.ast.Abstraction;
import pl.wojciechkarpiel.ast.Application;
import pl.wojciechkarpiel.ast.Constant;
import pl.wojciechkarpiel.ast.Variable;

public interface Visitor<T> {

    T visitConstant(Constant constant);

    T visitVariable(Variable variable);

    T visitApplication(Application application);

    T visitAbstraction(Abstraction abstraction);

}
