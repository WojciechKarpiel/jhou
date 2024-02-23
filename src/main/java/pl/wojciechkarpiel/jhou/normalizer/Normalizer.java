package pl.wojciechkarpiel.jhou.normalizer;

import pl.wojciechkarpiel.jhou.ast.*;
import pl.wojciechkarpiel.jhou.ast.type.ArrowType;
import pl.wojciechkarpiel.jhou.ast.util.Visitor;
import pl.wojciechkarpiel.jhou.termHead.BetaEtaNormal;
import pl.wojciechkarpiel.jhou.types.TypeCalculator;
import pl.wojciechkarpiel.jhou.util.MapUtil;

import java.util.HashMap;

public class Normalizer {

    public static Term betaNormalize(Term term) {
        return new Normalizer().betaNormalizeInternal(term);
    }

    public static Term etaContract(Term term) {
        return etaCompressInternal(term);
    }

    public static Term betaEtaNormalForm(Term term) {
        return BetaEtaNormal.normalize(term).backToTerm();
    }

    public static Term etaExpand(Term t) {
        ArrowType at = TypeCalculator.ensureArrowType(t);
        Variable v = Variable.freshVariable(at.getFrom());
        return new Abstraction(v, new Application(t, v));
    }

    private Normalizer() {
    }

    private static Term etaCompressInternal(Term term) {
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
                Term newFn = etaCompressInternal(application.getFunction());
                Term newArg = etaCompressInternal(application.getArgument());
                return new Application(newFn, newArg);
            }

            @Override
            public Term visitAbstraction(Abstraction abstraction) {
                Variable v = abstraction.getVariable();
                Term body = etaCompressInternal(abstraction.getBody());
                if (body instanceof Application) {
                    Application app = (Application) body;
                    if (v.equals(app.getArgument())) {
                        return etaCompressInternal(app.getFunction());
                    }
                }
                // nothing achieved;
                return new Abstraction(v, body);
            }
        });
    }

    private final MapUtil<Variable, Term> map = new MapUtil<>(new HashMap<>());

    private Term betaNormalizeInternal(Term term) {
        return term.visit(new Visitor<Term>() {
            public Term visitConstant(Constant constant) {
                return constant;
            }

            public Term visitVariable(Variable variable) {
                return map.get(variable).orElse(variable);
            }

            public Term visitApplication(Application application) {
                Term normalizedFunction = betaNormalizeInternal(application.getFunction());
                Term normalizedArgument = betaNormalizeInternal(application.getArgument());
                if (normalizedFunction instanceof Abstraction) {
                    Abstraction abstraction = (Abstraction) normalizedFunction;
                    return map.withMapping(
                            abstraction.getVariable(),
                            normalizedArgument,
                            () -> betaNormalizeInternal(abstraction.getBody())
                    );
                } else {
                    return new Application(normalizedFunction, normalizedArgument);
                }
            }

            public Term visitAbstraction(Abstraction abstraction) {
                Variable variable = abstraction.getVariable();
                Term normalizedBody = map.withoutMapping(variable, () -> betaNormalizeInternal(abstraction.getBody()));
                return new Abstraction(variable, normalizedBody);
            }
        });
    }
}
