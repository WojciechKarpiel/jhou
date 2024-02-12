package pl.wojciechkarpiel.normalizer;

import pl.wojciechkarpiel.ast.*;
import pl.wojciechkarpiel.ast.util.Visitor;
import pl.wojciechkarpiel.util.MapUtil;

import java.util.HashMap;

public class Normalizer {

    public static Term normalize(Term term) {
        return new Normalizer().normalizeInternal(term);
    }


    private Normalizer() {
    }

    private final MapUtil<Variable, Term> map = new MapUtil<>(new HashMap<>());

    private Term normalizeInternal(Term term) {
        return term.visit(new Visitor<Term>() {
            public Term visitConstant(Constant constant) {
                return constant;
            }

            public Term visitVariable(Variable variable) {
                return map.get(variable).orElse(variable);
            }

            public Term visitApplication(Application application) {
                Term normalizedFunction = normalizeInternal(application.getFunction());
                Term normalizedArgument = normalizeInternal(application.getArgument());
                if (normalizedFunction instanceof Abstraction) {
                    Abstraction abstraction = (Abstraction) normalizedFunction;
                    return map.withMapping(
                            abstraction.getVariable(),
                            normalizedArgument,
                            () -> normalizeInternal(abstraction.getBody())
                    );
                } else {
                    // TODO: is it allowed?
                    return new Application(normalizedFunction, normalizedArgument);
                }
            }

            public Term visitAbstraction(Abstraction abstraction) {
                Variable variable = abstraction.getVariable();
                Term normalizedBody = map.withoutMapping(variable, () -> normalizeInternal(abstraction.getBody()));
                return new Abstraction(variable, normalizedBody);
            }
        });
    }
}
