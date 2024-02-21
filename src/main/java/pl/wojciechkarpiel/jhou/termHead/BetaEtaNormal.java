package pl.wojciechkarpiel.jhou.termHead;

import pl.wojciechkarpiel.jhou.ast.*;
import pl.wojciechkarpiel.jhou.types.TypeCalculator;

import java.util.List;
import java.util.Objects;

public class BetaEtaNormal {

    public static BetaEtaNormal normalize(Term term) {
        return BetaEtaNormalizer.normalize(term);
    }

    public static BetaEtaNormal normalize(Term term, List<Variable> outsideBinders) {
        return BetaEtaNormalizer.normalize(term, outsideBinders);
    }

    // go through proper normalization because substitution could have broken eta
    public static BetaEtaNormal fromFakeNormal(Head hd, List<Variable> newRightBinders, List<Term> newRightArgs) {
        return normalize(new BetaEtaNormal(hd, newRightBinders, newRightArgs).backToTerm());
    }

    public boolean isRigid() {
        return head.visit(new Head.HeadVisitor<Boolean>() {
            @Override
            public Boolean visitConstant(Constant constant) {
                return true;
            }

            @Override
            public Boolean visitVariable(Variable variable) {
                return binder.contains(variable);
            }
        });
    }

    public Term backToTerm() {
        Term t = head.getTerm();
        for (Term argument : arguments) {
            t = new Application(t, argument);
        }
        for (int i = binder.size() - 1; i >= 0; i--) {
            t = new Abstraction(binder.get(i), t);
        }
        // sanity check
        TypeCalculator.calculateType(t);
        return t;
    }

    private final Head head;
    private final List<Variable> binder;
    private final List<Term> arguments;

    BetaEtaNormal(Head head, List<Variable> binder, List<Term> arguments) {
        this.head = head;
        this.binder = binder;
        this.arguments = arguments;
    }


    public List<Term> getArguments() {
        return arguments;
    }

    public List<Variable> getBinder() {
        return binder;
    }

    public Head getHead() {
        return head;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BetaEtaNormal that = (BetaEtaNormal) o;
        return Objects.equals(head, that.head) && Objects.equals(binder, that.binder) && Objects.equals(arguments, that.arguments);
        // TODO decide if BEN equality should be alpha. Counterpoint: Aplha-equalizing is core functionality of BEN, and making BEN alpha-equal would not allow to differentiate BENs before and after the core functionality is used
        // return to term for alpha-conversion handling
//        return backToTerm().equals(that.backToTerm());
    }

    @Override
    public int hashCode() {
        return Objects.hash(head, binder, arguments);
//        return backToTerm().hashCode();
    }

    @Override
    public String toString() {
        return "BetaEtaNormal{" +
                binder +
                "[" + head +
                "]" + arguments +
                '}';
    }
}
