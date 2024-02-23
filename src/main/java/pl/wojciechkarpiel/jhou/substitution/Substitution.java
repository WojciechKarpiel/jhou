package pl.wojciechkarpiel.jhou.substitution;

import pl.wojciechkarpiel.jhou.ast.*;
import pl.wojciechkarpiel.jhou.ast.util.Visitor;
import pl.wojciechkarpiel.jhou.normalizer.Normalizer;
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
            // might contain free variables if not beta-normalized. TODO: is it a sign of a bug?
            result = Normalizer.betaNormalize(new Substituter(substitutionPair).substituteInt(result));
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

    public boolean alphaEquals(Substitution that) {
        if (this.substitution.size() != that.substitution.size()) return false;
        for (int i = 0; i < substitution.size(); i++) {
            if (!this.substitution.get(i).alphaEquals(that.substitution.get(i))) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(substitution);
    }

    @Override
    public String toString() {
        return "Substitution{" + substitution + '}';
    }

    /**
     * Assigns a type to the variable
     *
     * @param variable Variable with inferred type
     * @return Variable of the same ID, and with the type assigned. If the input variable is typed, then acts as an identity function
     */
    public Variable regenerateType(Variable variable) {
        if (variable.getType() != null) return variable;
        for (SubstitutionPair pair : substitution) {
            Variable typedVariable = pair.getVariable();
            if (typedVariable.getId().equals(variable.getId())) {
                return typedVariable;
            }
        }
        throw new UnrecognizedUntypedVariableException(variable);
    }

    public static class UnrecognizedUntypedVariableException extends RuntimeException {
        private UnrecognizedUntypedVariableException(Variable variable) {
            super("Variable " + variable + " has no type and is not a part of the substitution");
        }
    }

    private class Substituter {
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
                public Term visitVariable(Variable variable_) {
                    Variable variable = Substitution.this.regenerateType(variable_);
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
                    Optional<SubstitutionPair> oldPair = Optional.empty();
                    if (v.equals(Substituter.this.substitution.getVariable())) {
                        oldPair = Optional.of(Substituter.this.substitution);
                        Substituter.this.substitution = new SubstitutionPair(v, v);
                    }
                    Term ret = new Abstraction(
                            abstraction.getVariable(),
                            substituteInt(abstraction.getBody())
                    );
                    oldPair.ifPresent(substitutionPair -> Substituter.this.substitution = substitutionPair);
                    return ret;
                }
            });
        }

    }
}
