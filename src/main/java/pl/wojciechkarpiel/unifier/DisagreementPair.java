package pl.wojciechkarpiel.unifier;

import pl.wojciechkarpiel.ast.Term;
import pl.wojciechkarpiel.termHead.BetaEtaNormal;

public class DisagreementPair {

    private final PairType type;

    private final BetaEtaNormal mostRigid;
    private final BetaEtaNormal leastRigid;

    public DisagreementPair(Term a, Term b) {
        this(BetaEtaNormal.normalize(a), BetaEtaNormal.normalize(b));
    }

    public DisagreementPair(BetaEtaNormal fst, BetaEtaNormal snd) {
        if (fst.isRigid() && snd.isRigid()) {
            type = PairType.RIGID_RIGID;
            mostRigid = fst;
            leastRigid = snd;
        } else if (fst.isRigid() || snd.isRigid()) {
            type = PairType.RIGID_FLEXIBLE;
            if (fst.isRigid()) {
                mostRigid = fst;
                leastRigid = snd;
            } else {
                mostRigid = snd;
                leastRigid = fst;
            }
        } else {
            type = PairType.FLEXIBLE_FLEXIBLE;
            mostRigid = fst;
            leastRigid = snd;
        }
    }


    // TODO cache
    public PairType getType() {
        return type;
    }

    public BetaEtaNormal getMostRigid() {
        return mostRigid;
    }

    public BetaEtaNormal getLeastRigid() {
        return leastRigid;
    }

    @Override
    public String toString() {
        return "DisagreementPair[" + type + "]:\n" +
                "  " + mostRigid +
                "\n  " + leastRigid;
    }
}
