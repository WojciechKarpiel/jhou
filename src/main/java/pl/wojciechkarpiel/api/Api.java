package pl.wojciechkarpiel.api;

import pl.wojciechkarpiel.ast.*;
import pl.wojciechkarpiel.ast.type.ArrowType;
import pl.wojciechkarpiel.ast.type.BaseType;
import pl.wojciechkarpiel.ast.type.Type;
import pl.wojciechkarpiel.normalizer.Normalizer;
import pl.wojciechkarpiel.unifier.SolutionIterator;
import pl.wojciechkarpiel.unifier.Unifier;

/**
 * Public API methods. The intended usage is:
 * <pre>
 *     import static pl.wojciechkarpiel.api.Api.*;
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

    public static Type arrowType(Type from, Type to) {
        return new ArrowType(from, to);
    }

    public static Term application(Term function, Term argument) {
        return new Application(function, argument);
    }

    public static Term abstraction(Variable variable, Term body) {
        return new Abstraction(variable, body);
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

    public static SolutionIterator unify(Term a, Term b) {
        return Unifier.unify(a, b);
    }

    public static SolutionIterator unify(Term a, Term b, int maxIterations) {
        return Unifier.unify(a, b, maxIterations);
    }
}
