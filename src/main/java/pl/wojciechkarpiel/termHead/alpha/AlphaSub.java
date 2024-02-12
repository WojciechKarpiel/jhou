package pl.wojciechkarpiel.termHead.alpha;

import pl.wojciechkarpiel.ast.Term;
import pl.wojciechkarpiel.ast.Variable;
import pl.wojciechkarpiel.substitution.Substitution;
import pl.wojciechkarpiel.termHead.BetaEtaNormal;
import pl.wojciechkarpiel.termHead.Head;
import pl.wojciechkarpiel.termHead.HeadOps;

import java.util.ArrayList;
import java.util.List;

public class AlphaSub {
    private AlphaSub() {
    }

    /**
     * requires the HEAD to be a var
     */
    public static BetaEtaNormal substitute(BetaEtaNormal ben, Variable newVar) {
        Variable substituted = HeadOps.asVariable(ben.getHead()).orElseThrow(CantSubstituteConstantException::new);

        int idx = ben.getBinder().lastIndexOf(substituted);
        if (idx < 0) throw new SubstitutedVariableNotRigidException();

        List<Variable> newBinders = new ArrayList<>(ben.getBinder().size());
        newBinders.addAll(ben.getBinder());
        newBinders.set(idx, newVar);

        if (newBinders.contains(substituted)) {
            System.out.println("[AlphaSub] SUS - variable shadowed, nothing to do");
            return new BetaEtaNormal(ben.getHead(), newBinders, ben.getArguments());
        }
        Substitution s = new Substitution(substituted, newVar);
        List<Term> newArgs = new ArrayList<>(ben.getArguments().size());
        ben.getArguments().forEach(arg -> newArgs.add(s.substitute(arg)));
        Head nh = new Head.HeadVariable(newVar);

        return new BetaEtaNormal(nh, newBinders, newArgs);
    }
}
