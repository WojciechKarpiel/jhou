package pl.wojciechkarpiel.termHead;

import pl.wojciechkarpiel.ast.Constant;
import pl.wojciechkarpiel.ast.Term;
import pl.wojciechkarpiel.ast.Variable;

import java.util.List;
import java.util.Objects;

public class BetaEtaNormal {

    public static BetaEtaNormal normalize(Term term) {
        return BetaEtaNormalizer.normalize(term);
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

    private final Head head;
    private final List<Variable> binder;
    private final List<Term> arguments;

    public BetaEtaNormal(Head head, List<Variable> binder, List<Term> arguments) {
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
    }

    @Override
    public int hashCode() {
        return Objects.hash(head, binder, arguments);
    }
}
