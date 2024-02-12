package pl.wojciechkarpiel.unifier.simplifier.result;

import pl.wojciechkarpiel.unifier.DisagreementSet;

public class SimplificationNode implements SimplificationResult {

    private final DisagreementSet disagreement;

    public SimplificationNode(DisagreementSet disagreement) {
        this.disagreement = disagreement;
    }

    public DisagreementSet getDisagreement() {
        return disagreement;
    }
}
