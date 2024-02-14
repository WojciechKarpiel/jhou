package pl.wojciechkarpiel.jhou.substitution;

import pl.wojciechkarpiel.jhou.ast.*;
import pl.wojciechkarpiel.jhou.ast.util.Visitor;
import pl.wojciechkarpiel.jhou.util.ListUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Substitution {

    private final List<SubstitutionPair> substitution;

    public Substitution(Variable variable, Term term) {
        this(ListUtil.of(new SubstitutionPair(variable, term)));
    }

    public Substitution(List<SubstitutionPair> substitution) {
        this.substitution = substitution;
    }

    public List<SubstitutionPair> getSubstitution() {
        return substitution;
    }

    public Term substitute(Term input) {
        return new Substituter(substitution).substituteInt(input);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Substitution that = (Substitution) o;
        return Objects.equals(substitution, that.substitution);
    }

    @Override
    public int hashCode() {
        return Objects.hash(substitution);
    }

    @Override
    public String toString() {
        return "Substitution{" + substitution + '}';
    }

    private static class Substituter {
        List<SubstitutionPair> substitution;

        private Substituter(List<SubstitutionPair> inSub) {
            // todo is defensive copy needed?
            this.substitution = new ArrayList<>(inSub);
        }

        public Term substituteInt(Term input) {
            return input.visit(new Visitor<Term>() {
                @Override
                public Term visitConstant(Constant constant) {
                    return constant;
                }

                @Override
                public Term visitVariable(Variable variable) {
                    return substitution.stream()
                            .filter(p -> p.getVariable().equals(variable))
                            .map(SubstitutionPair::getTerm)
                            .findFirst().orElse(variable);
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
                    Variable v = abstraction.getVariable();
                    int i;
                    Optional<SubstitutionPair> oldPair = Optional.empty();
                    for (i = 0; i < substitution.size(); i++) {
                        SubstitutionPair substitutionPair = substitution.get(i);
                        if (substitutionPair.getVariable().equals(v)) {
                            oldPair = Optional.of(substitutionPair);
                            substitution.set(i, new SubstitutionPair(v, v));
                            break;
                        }
                    }
                    Term ret = new Abstraction(
                            abstraction.getVariable(),
                            substituteInt(abstraction.getBody())
                    );
                    if (oldPair.isPresent()) {
                        substitution.set(i, oldPair.get());
                    }

                    return ret;
                }
            });
        }

    }
}
