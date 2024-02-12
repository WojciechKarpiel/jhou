package pl.wojciechkarpiel.termHead;

import pl.wojciechkarpiel.ast.Constant;
import pl.wojciechkarpiel.ast.Term;
import pl.wojciechkarpiel.ast.Variable;

import java.util.List;

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
}
