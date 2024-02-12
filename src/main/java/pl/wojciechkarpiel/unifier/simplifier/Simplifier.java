package pl.wojciechkarpiel.unifier.simplifier;

import pl.wojciechkarpiel.ast.Term;
import pl.wojciechkarpiel.termHead.BetaEtaNormal;
import pl.wojciechkarpiel.unifier.DisagreementPair;
import pl.wojciechkarpiel.unifier.DisagreementSet;
import pl.wojciechkarpiel.unifier.simplifier.result.NonUnifiable;
import pl.wojciechkarpiel.unifier.simplifier.result.SimplificationResult;
import pl.wojciechkarpiel.util.UnimplementedException;

public class Simplifier {
    private Simplifier() {
    }

    public static SimplificationResult simplify(DisagreementSet disagreements) {
        throw new UnimplementedException();
//        return null; // TODO
    }

    private SimplificationResult simplifyOne(DisagreementPair pair) {
        Term a = pair.getFirst();
        Term b = pair.getSecond();
        BetaEtaNormal aN = BetaEtaNormal.normalize(a);
        BetaEtaNormal bN = BetaEtaNormal.normalize(b);
        boolean aRigid = aN.isRigid();
        boolean bRigid = bN.isRigid();
        if (!aRigid && !bRigid) {
            // flexible-flexible
            throw new UnimplementedException();
//            Substitution s= null; // TODO
//            return new SimplificationSuccess(s);
        } else if (aRigid && bRigid) {


            if ((aN.getHead().getTerm().equals(bN.getHead().getTerm())) &&
                    (aN.getArguments().size() == bN.getArguments().size()) &&
                    (aN.getBinder().size() == bN.getBinder().size())) {
                // TODO try-to alpha-convert in case of var-var

                // todo gen disagreement pairs
                throw new UnimplementedException();
            } else {
                return NonUnifiable.INSTANCE;
            }
        } else {
            // rigid-flexible
            throw new UnimplementedException();
        }

    }
}
