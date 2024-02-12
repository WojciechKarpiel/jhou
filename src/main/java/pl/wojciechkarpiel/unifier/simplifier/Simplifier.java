package pl.wojciechkarpiel.unifier.simplifier;

import pl.wojciechkarpiel.ast.Constant;
import pl.wojciechkarpiel.ast.Term;
import pl.wojciechkarpiel.ast.Variable;
import pl.wojciechkarpiel.ast.type.Type;
import pl.wojciechkarpiel.ast.util.Id;
import pl.wojciechkarpiel.substitution.Substitution;
import pl.wojciechkarpiel.termHead.BetaEtaNormal;
import pl.wojciechkarpiel.termHead.HeadOps;
import pl.wojciechkarpiel.termHead.HeaderUnifier;
import pl.wojciechkarpiel.unifier.DisagreementPair;
import pl.wojciechkarpiel.unifier.DisagreementSet;
import pl.wojciechkarpiel.unifier.PairType;
import pl.wojciechkarpiel.unifier.simplifier.result.NonUnifiable;
import pl.wojciechkarpiel.unifier.simplifier.result.SimplificationResult;
import pl.wojciechkarpiel.unifier.simplifier.result.SimplificationSuccess;
import pl.wojciechkarpiel.util.UnimplementedException;

import java.util.*;

public class Simplifier {
    private Simplifier() {
    }

    public static SimplificationResult simplify(DisagreementSet disagreements) {

        List<DisagreementPair> ds = disagreements.getDisagreements();
        // 1. break down rigid-rigid
        while (ds.stream().anyMatch(d -> d.getType() == PairType.RIGID_RIGID)) {
            List<DisagreementPair> newL = new ArrayList<>();
            for (DisagreementPair d : ds) {
                Optional<List<DisagreementPair>> q = breakdownRigidRigid(d.getMostRigid(), d.getLeastRigid());
                if (q.isPresent()) {
                    newL.addAll(q.get());
                } else {
                    return NonUnifiable.INSTANCE;
                }
            }
            ds = newL;
        }
        ;


        // 2. check if its the end (flex-flex)
        if (ds.stream().allMatch(q -> q.getType() == PairType.FLEXIBLE_FLEXIBLE)) {
            Map<Variable, Term> fin = new HashMap<>();
            Map<Type, Constant> cs = new HashMap<>();
            for (DisagreementPair d : ds) {
                BetaEtaNormal aN = d.getMostRigid();
                BetaEtaNormal bN = d.getLeastRigid();
                Variable va = HeadOps.asVariable(aN.getHead()).get();
                Variable vb = HeadOps.asVariable(bN.getHead()).get();
                Type t = va.getType();
                Constant c = cs.putIfAbsent(t, new Constant(Id.uniqueId(), t));
                fin.put(va, c);
                fin.put(vb, c);
            }

            return new SimplificationSuccess(new Substitution(fin));
        }

        // 3. handle rigid-flex
        throw new UnimplementedException("RIGID-FLEX");

    }


    public static Optional<List<DisagreementPair>> breakdownRigidRigid(BetaEtaNormal a, BetaEtaNormal b) {
        BetaEtaNormal aN = (a);
        BetaEtaNormal bN = (b);

        if ((aN.getArguments().size() == bN.getArguments().size()) &&
                (aN.getBinder().size() == bN.getBinder().size())) {

            Optional<BetaEtaNormal> bnO = HeaderUnifier.alphaUnifyHeaderReturnNewRight(aN, bN);
            if (bnO.isPresent()) {
                BetaEtaNormal bNN = bnO.get();

                List<DisagreementPair> ds = new ArrayList<>();
                for (int i = 0; i < bNN.getArguments().size(); i++) {

                    DisagreementPair dp = new DisagreementPair(
                            extract(aN, aN.getArguments().get(i)),
                            extract(bNN, bNN.getArguments().get(i))
                    );
                    ds.add(dp); // TODO this is wrong pairs should be BetaEta with prefix, otherwise free variables here!
                }
                return Optional.of(ds);
            } else {
                return Optional.empty();
            }
        } else {
            throw new RuntimeException("WRONG TYPES!!!!!");
        }
    }

    private static BetaEtaNormal extract(BetaEtaNormal base, Term arg) {
        return BetaEtaNormal.normalize(arg, base.getBinder());
    }
}
