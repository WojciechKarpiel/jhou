package pl.wojciechkarpiel.jhou.api;

import pl.wojciechkarpiel.jhou.ast.*;
import pl.wojciechkarpiel.jhou.ast.type.ArrowType;
import pl.wojciechkarpiel.jhou.ast.type.BaseType;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.normalizer.Normalizer;
import pl.wojciechkarpiel.jhou.types.TypeCalculator;
import pl.wojciechkarpiel.jhou.unifier.SolutionIterator;
import pl.wojciechkarpiel.jhou.unifier.Unifier;

import java.util.function.Function;

/**
 * Public API methods. The intended usage is:
 * <pre>
 *     import static pl.wojciechkarpiel.jhou.api.Api.*;
 * </pre>
 */
public class Api {
    private Api() {
    }

    /**
     * @return fresh type
     */
    public static Type freshType() {
        return BaseType.freshBaseType();
    }

    /**
     * @return fresh variable of type `type`
     */
    public static Variable freshVariable(Type type) {
        return Variable.freshVariable(type);
    }

    /**
     * Name is only for pretty-printing, two fresh variables of the same name are distinct.
     * If you don't need pretty-printnig, use freshVariable(Type) variant
     *
     * @param name name of the variable
     * @return fresh variable
     */
    public static Variable freshVariable(Type type, String name) {
        return Variable.freshVariable(type, name);
    }

    public static Term freshConstant(Type type) {
        return Constant.freshConstant(type);
    }

    /**
     * Name is only for pretty-printing, two fresh variables of the same name are distinct.
     * If you don't need pretty-printnig, use freshConstant(Type) variant
     *
     * @param name name of the constant
     * @return fresh constant
     */
    public static Term freshConstant(Type type, String name) {
        return Constant.freshConstant(type, name);
    }

    public static Type arrow(Type from, Type to) {
        return new ArrowType(from, to);
    }

    public static Term application(Term function, Term argument) {
        return new Application(function, argument);
    }

    public static Term app(Term function, Term argument) {
        return application(function, argument);
    }

    public static Term abstraction(Type variableType, Function<Variable, Term> abstraction) {
        return Abstraction.fromLambda(variableType, abstraction);
    }

    public static Term abstraction(Type variableType, String variableName, Function<Variable, Term> abstraction) {
        return Abstraction.fromLambda(variableType, abstraction, variableName);
    }

    public static Term betaNormalize(Term term) {
        return Normalizer.betaNormalize(term);
    }

    public static Term etaNormalize(Term term) {
        return Normalizer.etaNormalize(term);
    }

    public static Term betaEtaNormalize(Term term) {
        return Normalizer.betaEtaNormalize(term);
    }

    public static Type typeOf(Term term) {
        return TypeCalculator.calculateType(term);
    }

    public static SolutionIterator unify(Term a, Term b) {
        return Unifier.unify(a, b);
    }

    public static SolutionIterator unify(Term a, Term b, int maxIterations) {
        return Unifier.unify(a, b, maxIterations);
    }
}
