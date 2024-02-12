package pl.wojciechkarpiel.unifier.simplifier;

import pl.wojciechkarpiel.ast.Constant;
import pl.wojciechkarpiel.ast.Term;
import pl.wojciechkarpiel.ast.Variable;
import pl.wojciechkarpiel.ast.util.Id;
import pl.wojciechkarpiel.substitution.Substitution;
import pl.wojciechkarpiel.termHead.BetaEtaNormal;
import pl.wojciechkarpiel.termHead.HeadOps;
import pl.wojciechkarpiel.termHead.HeaderUnifier;
import pl.wojciechkarpiel.unifier.DisagreementPair;
import pl.wojciechkarpiel.unifier.DisagreementSet;
import pl.wojciechkarpiel.unifier.simplifier.result.NonUnifiable;
import pl.wojciechkarpiel.unifier.simplifier.result.SimplificationNode;
import pl.wojciechkarpiel.unifier.simplifier.result.SimplificationResult;
import pl.wojciechkarpiel.unifier.simplifier.result.SimplificationSuccess;
import pl.wojciechkarpiel.util.UnimplementedException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Simplifier {
    private Simplifier() {
    }

    public static SimplificationResult simplify(DisagreementSet disagreements) {

        List<SimplificationResult> dd = disagreements.getDisagreements().stream().map(Simplifier::simplifyOne).collect(Collectors.toList());

        if (dd.stream().anyMatch(SimplificationResult::isFailure)) {
            return NonUnifiable.INSTANCE;
        }

        if (dd.stream().allMatch(d -> d instanceof SimplificationSuccess)) {
            // TODO merge map should be during handling initial
            Stream<Substitution> substitutionStream = dd.stream().map(d -> (SimplificationSuccess) d).map(d -> d.getSolution());
            Map<Variable, Term> r = new HashMap<>();
            substitutionStream.forEach(sub -> {
                sub.getSubstitution().keySet().forEach(k -> {
                    if (!r.containsKey(k) || r.get(k).equals(sub.getSubstitution().get(k))) {
                        r.put(k, sub.getSubstitution().get(k));
                    } else throw new RuntimeException("ŁĄCZENIE MAPEK");

                });
            });
            return new SimplificationSuccess(new Substitution(r));
        }

        // TODO backtracking and stuff
        throw new UnimplementedException();
    }

    public static SimplificationResult simplifyOne(DisagreementPair pair) {
        Term a = pair.getFirst();
        Term b = pair.getSecond();
        BetaEtaNormal aN = BetaEtaNormal.normalize(a);
        BetaEtaNormal bN = BetaEtaNormal.normalize(b);
        boolean aRigid = aN.isRigid();
        boolean bRigid = bN.isRigid();
        if (!aRigid && !bRigid) {
            // flexible-flexible
            Map<Variable, Term> m = new HashMap<>();
            // must be variables if flex-flex
            Variable aa = HeadOps.asVariable(aN.getHead()).get();
            Variable bb = HeadOps.asVariable(aN.getHead()).get();
            Constant fresh = new Constant(Id.uniqueId(), aa.getType());
            m.put(aa, fresh);
            m.put(bb, fresh);
            return new SimplificationSuccess(new Substitution(m));
        } else if (aRigid && bRigid) {
            if ((aN.getArguments().size() == bN.getArguments().size()) &&
                    (aN.getBinder().size() == bN.getBinder().size())) {

                Optional<BetaEtaNormal> bnO = HeaderUnifier.alphaUnifyHeaderReturnNewRight(aN, bN);
                if (bnO.isPresent()) {
                    BetaEtaNormal bNN = bnO.get();

                    List<DisagreementPair> ds = new ArrayList<>();
                    for (int i = 0; i < bNN.getArguments().size(); i++) {
                        DisagreementPair dp = new DisagreementPair(
                                aN.getArguments().get(i),
                                bNN.getArguments().get(i)
                        );
                        ds.add(dp);
                    }
                    return SimplificationNode.fromDisagreements(new DisagreementSet(ds));


                } else {
                    return NonUnifiable.INSTANCE;
                }

            } else {
                throw new RuntimeException("WRONG TYPES!!!!!");
            }
        } else {
            // rigid-flexible
            throw new UnimplementedException("RIGID-FLEXIBLE, THE BIG BOSS");
        }

    }
}
