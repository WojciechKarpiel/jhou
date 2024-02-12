package pl.wojciechkarpiel.unifier;

import pl.wojciechkarpiel.ast.Term;
import pl.wojciechkarpiel.termHead.BetaEtaNormal;

public class DisagreementPair {

    private final PairType type;

    private final Term mostRigid;
    private final Term leastRigid;

    public DisagreementPair(Term first, Term second) {
        BetaEtaNormal fst = BetaEtaNormal.normalize(first);
        BetaEtaNormal snd = BetaEtaNormal.normalize(second);
        if (fst.isRigid() && snd.isRigid()) {
            type = PairType.RIGID_RIGID;
            mostRigid = first;
            leastRigid = second;
        } else if (fst.isRigid() || snd.isRigid()) {
            type = PairType.RIGID_FLEXIBLE;
            if (fst.isRigid()) {
                mostRigid = first;
                leastRigid = second;
            } else {
                mostRigid = second;
                leastRigid = first;
            }
        } else {
            type = PairType.FLEXIBLE_FLEXIBLE;
            mostRigid = first;
            leastRigid = second;
        }
    }


    // TODO cache
    public PairType getType() {
        return type;
    }

    public Term getMostRigid() {
        return mostRigid;
    }

    public Term getLeastRigid() {
        return leastRigid;
    }
}
