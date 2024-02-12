package pl.wojciechkarpiel.substitution;

import pl.wojciechkarpiel.ast.*;
import pl.wojciechkarpiel.ast.util.Visitor;
import pl.wojciechkarpiel.util.MapUtil;

import java.util.HashMap;
import java.util.Map;

public class Substitution {

    private final Map<Variable, Term> substitution;

    public Substitution(Variable variable, Term term) {
        Map<Variable, Term> sub = new HashMap<>();
        sub.put(variable, term);
        this.substitution = sub;
    }

    public Substitution(Map<Variable, Term> substitution) {
        this.substitution = substitution;
    }

    public Map<Variable, Term> getSubstitution() {
        return substitution;
    }

    public Term substitute(Term input) {
        return new Substitution.Substituter(substitution).substituteInt(input);
    }


    private static class Substituter {
        MapUtil<Variable, Term> map;

        private Substituter(Map<Variable, Term> inSub) {
            // todo is defensive copy needed?
            this.map = new MapUtil<>(new HashMap<>(inSub));
        }

        public Term substituteInt(Term input) {
            return input.visit(new Visitor<Term>() {
                @Override
                public Term visitConstant(Constant constant) {
                    return constant;
                }

                @Override
                public Term visitVariable(Variable variable) {
                    return map.get(variable).orElse(variable);
                }

                @Override
                public Term visitApplication(Application application) {
                    return new Application(
                            substituteInt(application.getFunction()),
                            substituteInt(application.getArgument())
                    );
                }

                @Override
                public Term visitAbstraction(Abstraction abstraction) {
                    return new Abstraction(
                            abstraction.getVariable(),
                            map.withoutMapping(
                                    abstraction.getVariable(),
                                    () -> substituteInt(abstraction.getBody())
                            )
                    );
                }
            });
        }

    }
}
