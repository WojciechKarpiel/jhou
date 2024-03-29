package pl.wojciechkarpiel.jhou.unifier.simplifier;

import pl.wojciechkarpiel.jhou.alpha.AlphaEqual;
import pl.wojciechkarpiel.jhou.ast.Constant;
import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.ast.Variable;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.ast.util.Id;
import pl.wojciechkarpiel.jhou.substitution.Substitution;
import pl.wojciechkarpiel.jhou.substitution.SubstitutionPair;
import pl.wojciechkarpiel.jhou.termHead.BetaEtaNormal;
import pl.wojciechkarpiel.jhou.termHead.HeadOps;
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
                if (d.getType() == PairType.RIGID_RIGID) {
                    Optional<List<DisagreementPair>> q = breakdownRigidRigid(d.getMostRigid(), d.getLeastRigid());
                    if (q.isPresent()) {
                        newL.addAll(q.get());
                    } else {
                        return NonUnifiable.INSTANCE;
                    }
                } else {
                    newL.add(d);
                }
            }
            ds = newL;
        }


        // 2. check if it's the end (flex-flex)
        if (ds.stream().allMatch(q -> q.getType() == PairType.FLEXIBLE_FLEXIBLE)) {
            List<SubstitutionPair> fin = new ArrayList<>();
            Map<Type, Constant> cs = new HashMap<>();
            for (DisagreementPair d : ds) {
                BetaEtaNormal aN = d.getMostRigid();
                BetaEtaNormal bN = d.getLeastRigid();
                Variable va = HeadOps.asVariableYolo(aN.getHead());
                Variable vb = HeadOps.asVariableYolo(bN.getHead());
                Type t = va.getType();
                cs.putIfAbsent(t, new Constant(Id.uniqueId(), t));
                Constant c = cs.get(t);
                fin.add(new SubstitutionPair(va, c));
                fin.add(new SubstitutionPair(vb, c));
            }

            return new SimplificationSuccess(new Substitution(fin));
        }

        // 3. handle rigid-flex
        // lol jk not doing that here
        return new SimplificationNode(new DisagreementSet(ds));
    }

    public static Optional<List<DisagreementPair>> breakdownRigidRigid(BetaEtaNormal a_, BetaEtaNormal b_) {
        Optional<AlphaEqual.BenPair> benPair = AlphaEqual.alphaEqualizeHeading(a_, b_);
        if (!benPair.isPresent()) return Optional.empty();
        BetaEtaNormal a = benPair.get().getLeft();
        BetaEtaNormal b = benPair.get().getRight();

        List<DisagreementPair> ds = new ArrayList<>();
        for (int i = 0; i < b.getArguments().size(); i++) {

            DisagreementPair dp = new DisagreementPair(
                    extract(a, a.getArguments().get(i)),
                    extract(b, b.getArguments().get(i))
            );
            ds.add(dp);
        }

        return Optional.of(ds);
    }

    private static BetaEtaNormal extract(BetaEtaNormal base, Term arg) {
        return BetaEtaNormal.normalize(arg, base.getBinder());
    }
}
