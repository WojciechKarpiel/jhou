package pl.wojciechkarpiel.jhou.unifier.simplifier;

import pl.wojciechkarpiel.jhou.ast.Constant;
import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.ast.Variable;
import pl.wojciechkarpiel.jhou.ast.type.ArrowType;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.ast.util.Id;
import pl.wojciechkarpiel.jhou.substitution.Substitution;
import pl.wojciechkarpiel.jhou.termHead.BetaEtaNormal;
import pl.wojciechkarpiel.jhou.termHead.Head;
import pl.wojciechkarpiel.jhou.termHead.HeadOps;
import pl.wojciechkarpiel.jhou.types.TypeCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class Matcher {
    public static class RigidFlexible {
        private final BetaEtaNormal rigid;
        private final BetaEtaNormal flexible;

        public RigidFlexible(BetaEtaNormal rigid, BetaEtaNormal flexible) {
            this.rigid = rigid;
            this.flexible = flexible;
            if (!rigid.isRigid()) throw new IllegalArgumentException();
            if (flexible.isRigid()) throw new IllegalArgumentException();
        }

        public BetaEtaNormal getFlexible() {
            return flexible;
        }

        public BetaEtaNormal getRigid() {
            return rigid;
        }


        // below methods use names from paper, eases confusion when translating paper into code, will refactor later TODO
        public int getN() {
            return getRigid().getBinder().size();
        }

        public int getP() {
            return getFlexible().getArguments().size();
        }

        public List<Term> underP() {
            return getFlexible().getArguments();
        }

        public int getQ() {
            return getRigid().getArguments().size();
        }

        public List<Term> underQ() {
            return getRigid().getArguments();
        }
    }


    public static List<Substitution> matchS(BetaEtaNormal rigid, BetaEtaNormal flexible) {
        List<Term> matches = match(rigid, flexible);
        Variable v = HeadOps.asVariable(flexible.getHead()).get();
        return matches.stream().map(m -> new Substitution(v, m)).collect(Collectors.toList());
    }
    /**
     * @return possible solutions
     */
    public static List<Term> match(BetaEtaNormal rigid, BetaEtaNormal flexible) {
        return match(new RigidFlexible(rigid, flexible));
    }

    public static List<Term> match(RigidFlexible rigidFlexible) {
        BetaEtaNormal rigid = rigidFlexible.getRigid();
        BetaEtaNormal flexible = rigidFlexible.getFlexible();
        List<Term> res = new ArrayList<>();
        Optional<Constant> rigidConstant = HeadOps.asConstant(rigid.getHead());
        rigidConstant.ifPresent(c -> res.add(imitate(rigidFlexible)));

        res.addAll(projections(rigidFlexible));

        return res;
    }

    public static List<Term> projections(RigidFlexible rigidFlexible) {
        List<Term> res = new ArrayList<>(rigidFlexible.getP());
        for (int i = 0; i < rigidFlexible.getP(); i++) {
            res.add(projForBinder(i, rigidFlexible));
        }

        return res;
    }

    private static Term projForBinder(int i, RigidFlexible rigidFlexible) {
        List<Variable> binders = getPBinders(rigidFlexible);
        Head.HeadVariable newHead = new Head.HeadVariable(binders.get(i));
        Type headType = newHead.getV().getType();
        List<Term> args = new ArrayList<>(headType.arity());
        for (int j = 0; j < headType.arity(); j++) {
            Type targetType = headType;
            for (int q = 0; q < j + 1; q++) { //todo test this its yolo
                targetType = ((ArrowType) targetType).getFrom();
            }

            for (int k = binders.size() - 1; k >= 0; k--) {
                targetType = new ArrowType(binders.get(k).getType(), targetType);
            }
            Variable hi = new Variable(Id.uniqueId(), targetType);
            BetaEtaNormal arg = BetaEtaNormal.fromFakeNormal(
                    new Head.HeadVariable(hi),
                    new ArrayList<>(),
                    new ArrayList<>(binders) // replacement binders are arg's argyments
            );
            TypeCalculator.calculateType(arg.backToTerm()); //sanity check
            args.add(arg.backToTerm());
        }
        Term term = BetaEtaNormal.fromFakeNormal(newHead, binders, args).backToTerm();
        TypeCalculator.calculateType(term);
        return term;
    }

    private static List<Variable> getPBinders(RigidFlexible rigidFlexible) {
        List<Variable> binders = new ArrayList<>(rigidFlexible.getP());
        for (Term flexibleArg : rigidFlexible.underP()) {
            Variable v = new Variable(Id.uniqueId(), TypeCalculator.calculateType(flexibleArg));
            binders.add(v);
        }
        return binders;
    }

    public static Term imitate(RigidFlexible rigidFlexible) {
        List<Variable> binders = getPBinders(rigidFlexible);
        Head newHead = rigidFlexible.getRigid().getHead();
        List<Term> args = new ArrayList<>(rigidFlexible.getQ());
        for (Term rigidArg : rigidFlexible.underQ()) {
            Type targetType = TypeCalculator.calculateType(rigidArg);
            for (int i = binders.size() - 1; i >= 0; i--) {
                targetType = new ArrowType(binders.get(i).getType(), targetType);
            }
            Variable hi = new Variable(Id.uniqueId(), targetType);
            BetaEtaNormal arg = BetaEtaNormal.fromFakeNormal(
                    new Head.HeadVariable(hi),
                    new ArrayList<>(),
                    new ArrayList<>(binders) // replacement binders are arg's arguments
            );
            TypeCalculator.calculateType(arg.backToTerm()); //sanity check
            args.add(arg.backToTerm());
        }

        Term term = BetaEtaNormal.fromFakeNormal(newHead, binders, args).backToTerm();
        TypeCalculator.calculateType(term);
        return term;
    }
}
