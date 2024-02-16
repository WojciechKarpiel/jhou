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

    public static Substitution empty() {
        return new Substitution(new ArrayList<>());
    }

    public List<SubstitutionPair> getSubstitution() {
        return substitution;
    }

    public Term substitute(Term input) {
        Term result = input;
        for (SubstitutionPair substitutionPair : substitution) {
            result = new Substituter(substitutionPair).substituteInt(result);
        }
        return result;
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
        SubstitutionPair substitution;

        private Substituter(SubstitutionPair inSub) {
            this.substitution = inSub;
        }

        public Term substituteInt(Term input) {
            return input.visit(new Visitor<Term>() {
                @Override
                public Term visitConstant(Constant constant) {
                    return constant;
                }

                @Override
                public Term visitVariable(Variable variable) {
                    if (Substituter.this.substitution.getVariable().equals(variable)) {
                        return Substituter.this.substitution.getTerm();
                    } else {
                        return variable;
                    }
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
                    if (v.equals(Substituter.this.substitution.getVariable())) {
                        oldPair = Optional.of(Substituter.this.substitution);
                        Substituter.this.substitution = new SubstitutionPair(v, v);
                    }
                    Term ret = new Abstraction(
                            abstraction.getVariable(),
                            substituteInt(abstraction.getBody())
                    );
                    if (oldPair.isPresent()) {
                        Substituter.this.substitution = oldPair.get();
                    }
                    return ret;
                }
            });
        }

    }
}
