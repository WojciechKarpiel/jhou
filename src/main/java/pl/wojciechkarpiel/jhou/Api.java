package pl.wojciechkarpiel.jhou;

import pl.wojciechkarpiel.jhou.ast.*;
import pl.wojciechkarpiel.jhou.ast.type.ArrowType;
import pl.wojciechkarpiel.jhou.ast.type.BaseType;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.normalizer.Normalizer;
import pl.wojciechkarpiel.jhou.types.TypeCalculator;
import pl.wojciechkarpiel.jhou.unifier.SolutionIterator;
import pl.wojciechkarpiel.jhou.unifier.UnificationSettings;
import pl.wojciechkarpiel.jhou.unifier.Unifier;

import java.util.function.Function;

/**
 * Public API methods. See <a href="https://github.com/WojciechKarpiel/jhou">README</a> for reference. The intended usage is:
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
     * Name is only for pretty-printing, two fresh constants of the same name are distinct.
     * If you don't need pretty-printnig, use freshConstant(Type) variant
     *
     * @param name name of the constant
     * @return fresh constant
     */
    public static Type freshType(String name) {
        return BaseType.freshBaseType(name);
    }

    /**
     * @return fresh variable of type `type`
     */
    public static Variable freshVariable(Type type) {
        return Variable.freshVariable(type);
    }

    /**
     * Type will be automatically inferred during unification
     */
    public static Variable freshVariable() {
        return Variable.freshVariable(null);
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

    /**
     * Type will be automatically inferred during unification
     */
    public static Variable freshVariable(String name) {
        return Variable.freshVariable(null, name);
    }

    public static Term freshConstant(Type type) {
        return Constant.freshConstant(type);
    }

    /**
     * Type will be automatically inferred during unification
     */
    public static Term freshConstant() {
        return Constant.freshConstant(null);
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

    /**
     * Type will be automatically inferred during unification
     */
    public static Term freshConstant(String name) {
        return Constant.freshConstant(null, name);
    }

    public static Type arrow(Type from, Type to) {
        return new ArrowType(from, to);
    }

    public static Type typeOfCurriedFunction(Type arg1, Type arg2, Type... argN) {
        return ArrowType.typeOfCurriedFunction(arg1, arg2, argN);
    }

    public static Term application(Term function, Term argument, Term... moreArguments) {
        return Application.apply(function, argument, moreArguments);
    }

    public static Term app(Term function, Term argument, Term... moreArguments) {
        return application(function, argument, moreArguments);
    }

    public static Term abstraction(Type variableType, Function<Variable, Term> abstraction) {
        return Abstraction.fromLambda(variableType, abstraction);
    }

    /**
     * Type will be automatically inferred during unification
     */
    public static Term abstraction(Function<Variable, Term> abstraction) {
        return Abstraction.fromLambda(null, abstraction);
    }

    public static Term abstraction(Type variableType, String variableName, Function<Variable, Term> abstraction) {
        return Abstraction.fromLambda(variableType, abstraction, variableName);
    }

    /**
     * Type will be automatically inferred during unification
     */
    public static Term abstraction(String variableName, Function<Variable, Term> abstraction) {
        return Abstraction.fromLambda(null, abstraction, variableName);
    }

    public static Term betaNormalize(Term term) {
        return Normalizer.betaNormalize(term);
    }

    /**
     * Simplifies every occurrence of λx.fx  into f.
     * Note that this is different from the eta-transformation
     * in the beta-eta normal form
     *
     * @return term with every occurrence of λx.fx simplified into f
     */
    public static Term etaContract(Term term) {
        return Normalizer.etaContract(term);
    }

    public static Term etaExpand(Term term) {
        return Normalizer.etaExpand(term);
    }

    /**
     * @return term in beta-eta normal form
     */
    public static Term betaEtaNormalForm(Term term) {
        return Normalizer.betaEtaNormalForm(term);
    }

    public static Term betaNormalEtaContracted(Term term) {
        return etaContract(betaNormalize(term));
    }

    public static Type typeOf(Term term) {
        return TypeCalculator.calculateType(term);
    }

    public static boolean alphaEqual(Term a, Term b) {
        return Equality.alphaEqual(a, b);
    }

    public static boolean alphaBetaEtaEqual(Term a, Term b) {
        return Equality.alphaBetaEtaEqual(a, b);
    }

    public static SolutionIterator unify(Term a, Term b) {
        return Unifier.unify(a, b);
    }

    public static SolutionIterator unify(Term a, Term b, UnificationSettings unificationSettings) {
        return Unifier.unify(a, b, unificationSettings);
    }
}
