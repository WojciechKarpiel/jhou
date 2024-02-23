package pl.wojciechkarpiel.jhou.ast;

import pl.wojciechkarpiel.jhou.alpha.AlphaEqual;
import pl.wojciechkarpiel.jhou.normalizer.Normalizer;

public class Equality {
    private Equality() {
    }

    public static boolean alphaEqual(Term a, Term b) {
        return AlphaEqual.isAlphaEqual(a, b);
    }

    public static boolean alphaBetaEtaEqual(Term a, Term b) {
        return alphaEqual(
                Normalizer.etaContract(Normalizer.betaNormalize(a)),
                Normalizer.etaContract(Normalizer.betaNormalize(b))
        );
    }
}
