package pl.wojciechkarpiel.jhou.unifier.simplifier;

import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.ast.Variable;
import pl.wojciechkarpiel.jhou.ast.type.ArrowType;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.termHead.BetaEtaNormal;
import pl.wojciechkarpiel.jhou.termHead.Head;
import pl.wojciechkarpiel.jhou.termHead.HeadOps;
import pl.wojciechkarpiel.jhou.types.TypeCalculator;

import java.util.ArrayList;
import java.util.List;


public class Matcher {
    /**
     * The "match" procedure from paper (section 3.4)
     *
     * @return possible substitutions that *might* unify the input terms
     */
    public static PossibleSubstitutions match(BetaEtaNormal rigid, BetaEtaNormal flexible) {
        List<Term> mightUnify = possibleSubstitutionTerms(new RigidFlexible(rigid, flexible));
        Variable substitutedVariable = HeadOps.asVariableYolo(flexible.getHead());
        return new PossibleSubstitutions(substitutedVariable, mightUnify);
    }

    /**
     * @return possible solutions that *might* unify the two sides
     */
    static List<Term> possibleSubstitutionTerms(RigidFlexible rigidFlexible) {
        BetaEtaNormal rigid = rigidFlexible.getRigid();
        List<Term> result = new ArrayList<>();
        HeadOps.asConstant(rigid.getHead()).ifPresent(c -> result.add(imitation(rigidFlexible)));
        result.addAll(projections(rigidFlexible));
        return result;
    }

    /**
     * Imitation - see section 3.4 of the paper
     */
    private static Term imitation(RigidFlexible rigidFlexible) {
        List<Variable> binders = bindersForEatingUpFlexibleArguments(rigidFlexible);
        Head newHead = rigidFlexible.getRigid().getHead();
        List<Term> args = new ArrayList<>(rigidFlexible.getRigid().getArguments().size());
        for (Term rigidArg : rigidFlexible.getRigid().getArguments()) {
            args.add(argumentOfArgument(binders, rigidArg));
        }
        return BetaEtaNormal.fromFakeNormal(newHead, binders, args).backToTerm();
    }

    /**
     * Projections - see section 3.4 of the paper
     */
    private static List<Term> projections(RigidFlexible rigidFlexible) {
        List<Term> result = new ArrayList<>(rigidFlexible.getFlexible().getArguments().size());
        for (int i = 0; i < rigidFlexible.getFlexible().getArguments().size(); i++) {
            Term projection = projectionForBinder(i, rigidFlexible);
            Type projectionType = TypeCalculator.calculateType(projection);
            Type expectedType = TypeCalculator.calculateType(rigidFlexible.flexible.getHead().getTerm());
            //  it's not mentioned in paper, but it can (and likely will) happen that the type of arg is different from content
            if (projectionType.equals(expectedType)) {
                result.add(projection);
            }
        }
        return result;
    }

    private static Term projectionForBinder(int binderIndex, RigidFlexible rigidFlexible) {
        List<Variable> binders = bindersForEatingUpFlexibleArguments(rigidFlexible);
        Head.HeadVariable newHead = new Head.HeadVariable(binders.get(binderIndex));
        BetaEtaNormal benHeadBinder = BetaEtaNormal.normalize(binders.get(binderIndex));
        Type headType = newHead.getVariable().getType();
        List<Term> args = new ArrayList<>(headType.arity());
        for (int i = 0; i < headType.arity(); i++) {
            args.add(argumentOfArgument(binders, benHeadBinder.getArguments().get(i)));
        }
        return BetaEtaNormal.fromFakeNormal(newHead, binders, args).backToTerm();
    }

    private static List<Variable> bindersForEatingUpFlexibleArguments(RigidFlexible rigidFlexible) {
        List<Variable> binders = new ArrayList<>(rigidFlexible.getFlexible().getArguments().size());
        for (Term flexibleArg : rigidFlexible.getFlexible().getArguments()) {
            binders.add(Variable.freshVariable(TypeCalculator.calculateType(flexibleArg)));
        }
        return binders;
    }

    private static Term argumentOfArgument(List<Variable> binders, Term realArgUponWhichTheArgArgIsBased) {
        Type targetType = TypeCalculator.calculateType(realArgUponWhichTheArgArgIsBased);
        for (int i = binders.size() - 1; i >= 0; i--) {
            targetType = new ArrowType(binders.get(i).getType(), targetType);
        }
        BetaEtaNormal argument = BetaEtaNormal.fromFakeNormal(
                new Head.HeadVariable(Variable.freshVariable(targetType)),
                new ArrayList<>(),
                new ArrayList<>(binders) // replacement binders are arg's arguments
        );
        return argument.backToTerm();
    }

    static class RigidFlexible {
        private final BetaEtaNormal rigid;
        private final BetaEtaNormal flexible;

        RigidFlexible(BetaEtaNormal rigid, BetaEtaNormal flexible) {
            this.rigid = rigid;
            this.flexible = flexible;
            if (!rigid.isRigid())
                throw new IllegalArgumentException();
            if (flexible.isRigid())
                throw new IllegalArgumentException();
        }

        BetaEtaNormal getFlexible() {
            return flexible;
        }

        BetaEtaNormal getRigid() {
            return rigid;
        }
    }
}
