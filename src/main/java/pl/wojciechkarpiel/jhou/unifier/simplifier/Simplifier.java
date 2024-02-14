package pl.wojciechkarpiel.jhou.unifier.simplifier;

import pl.wojciechkarpiel.jhou.ast.Constant;
import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.ast.Variable;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.ast.util.Id;
import pl.wojciechkarpiel.jhou.substitution.Substitution;
import pl.wojciechkarpiel.jhou.substitution.SubstitutionPair;
import pl.wojciechkarpiel.jhou.termHead.BetaEtaNormal;
import pl.wojciechkarpiel.jhou.termHead.HeadOps;
import pl.wojciechkarpiel.jhou.termHead.HeaderUnifier;
import pl.wojciechkarpiel.jhou.unifier.DisagreementPair;
import pl.wojciechkarpiel.jhou.unifier.DisagreementSet;
import pl.wojciechkarpiel.jhou.unifier.PairType;
import pl.wojciechkarpiel.jhou.unifier.simplifier.result.NonUnifiable;
import pl.wojciechkarpiel.jhou.unifier.simplifier.result.SimplificationNode;
import pl.wojciechkarpiel.jhou.unifier.simplifier.result.SimplificationResult;
import pl.wojciechkarpiel.jhou.unifier.simplifier.result.SimplificationSuccess;

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
            List<SubstitutionPair> fin = new ArrayList<>();
            Map<Type, Constant> cs = new HashMap<>();
            for (DisagreementPair d : ds) {
                BetaEtaNormal aN = d.getMostRigid();
                BetaEtaNormal bN = d.getLeastRigid();
                Variable va = HeadOps.asVariable(aN.getHead()).get();
                Variable vb = HeadOps.asVariable(bN.getHead()).get();
                Type t = va.getType();
                Constant c = cs.putIfAbsent(t, new Constant(Id.uniqueId(), t));
                fin.add(new SubstitutionPair(va, c));
                fin.add(new SubstitutionPair(vb, c));
            }

            return new SimplificationSuccess(new Substitution(fin));
        }

        // 3. handle rigid-flex
        // lol jk not doing that here
        return new SimplificationNode(new DisagreementSet(ds));
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
                {
                    System.out.println("Broke down ");
                    System.out.println(a);
                    System.out.println(b);
                    System.out.println("into following");
                    ds.forEach(System.out::println);
                }

                return Optional.of(ds);
            } else {
                return Optional.empty();
            }
        } else {
            System.out.println("Total mismatch, might be caused by  nonverified matcher projection, nagmi");
            System.out.println(aN);
            System.out.println(bN);
            System.out.println("---");
            return Optional.empty();
        }
    }

    private static BetaEtaNormal extract(BetaEtaNormal base, Term arg) {
        return BetaEtaNormal.normalize(arg, base.getBinder());
    }
}
