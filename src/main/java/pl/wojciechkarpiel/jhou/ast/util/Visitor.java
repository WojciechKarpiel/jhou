package pl.wojciechkarpiel.jhou.ast.util;

import pl.wojciechkarpiel.jhou.ast.Abstraction;
import pl.wojciechkarpiel.jhou.ast.Application;
import pl.wojciechkarpiel.jhou.ast.Constant;
import pl.wojciechkarpiel.jhou.ast.Variable;

public interface Visitor<T> {

    T visitConstant(Constant constant);

    T visitVariable(Variable variable);

    T visitApplication(Application application);

    T visitAbstraction(Abstraction abstraction);

}
