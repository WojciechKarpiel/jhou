package pl.wojciechkarpiel.jhou.ast;

import pl.wojciechkarpiel.jhou.normalizer.Normalizer;

public class Equality {
    private Equality() {
    }

    public static boolean alphaEqual(Term a, Term b) {
        return a.equals(b);
    }

    public static boolean alphaBetaEtaEqual(Term a, Term b) {
        // instead of eta-contracting a beta-normal form, we could beta-eta normalize
        // former option seems much more straightforward, both should be equivalent
        return alphaEqual(
                Normalizer.etaContract(Normalizer.betaNormalize(a)),
                Normalizer.etaContract(Normalizer.betaNormalize(b))
        );
    }
}
