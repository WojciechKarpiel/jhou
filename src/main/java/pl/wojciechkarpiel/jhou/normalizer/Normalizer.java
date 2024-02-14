package pl.wojciechkarpiel.jhou.normalizer;

import pl.wojciechkarpiel.jhou.ast.*;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.ast.util.Visitor;
import pl.wojciechkarpiel.jhou.types.TypeCalculator;
import pl.wojciechkarpiel.jhou.util.MapUtil;

import java.util.HashMap;

public class Normalizer {

    public static Term betaNormalize(Term term) {
        // sanity check to move somewhere else
        TypeCalculator.calculateType(term);
        return new Normalizer().betaInternal(term);
    }

    public static Term etaNormalize(Term term) {
        Type pre = TypeCalculator.calculateType(term);
        Term normalized = etaNormalizeInternal(term);
        Type post = TypeCalculator.calculateType(normalized);
        if (!pre.equals(post)) { //sanity check
            throw new RuntimeException();
        }
        return normalized;
    }

    public static Term betaEtaNormalize(Term term) {
        return etaNormalize(betaNormalize(term));
    }

    private Normalizer() {
    }

    private static Term etaNormalizeInternal(Term term) {
        return term.visit(new Visitor<Term>() {
            @Override
            public Term visitConstant(Constant constant) {
                return constant;
            }

            @Override
            public Term visitVariable(Variable variable) {
                return variable;
            }

            @Override
            public Term visitApplication(Application application) {
                Term newFn = etaNormalizeInternal(application.getFunction());
                Term newArg = etaNormalizeInternal(application.getArgument());
                return new Application(newFn, newArg);
            }

            @Override
            public Term visitAbstraction(Abstraction abstraction) {
                // DETECT ETA possiblility
                Variable v = abstraction.getVariable();
                Term body = etaNormalizeInternal(abstraction.getBody());
                if (body instanceof Application) {
                    Application app = (Application) body;
                    if (app.getArgument().equals(v)) {
                        return etaNormalizeInternal(app.getFunction()); // lol can eta
                    }
                }
                // nothing achieved;
                return new Abstraction(v, body);
            }
        });
    }

    private final MapUtil<Variable, Term> map = new MapUtil<>(new HashMap<>());

    private Term betaInternal(Term term) {
        return term.visit(new Visitor<Term>() {
            public Term visitConstant(Constant constant) {
                return constant;
            }

            public Term visitVariable(Variable variable) {
                return map.get(variable).orElse(variable);
            }

            public Term visitApplication(Application application) {
                Term normalizedFunction = betaInternal(application.getFunction());
                Term normalizedArgument = betaInternal(application.getArgument());
                if (normalizedFunction instanceof Abstraction) {
                    Abstraction abstraction = (Abstraction) normalizedFunction;
                    return map.withMapping(
                            abstraction.getVariable(),
                            normalizedArgument,
                            () -> betaInternal(abstraction.getBody())
                    );
                } else {
                    // TODO: is it allowed?
                    return new Application(normalizedFunction, normalizedArgument);
                }
            }

            public Term visitAbstraction(Abstraction abstraction) {
                Variable variable = abstraction.getVariable();
                Term normalizedBody = map.withoutMapping(variable, () -> betaInternal(abstraction.getBody()));
                return new Abstraction(variable, normalizedBody);
            }
        });
    }
}
