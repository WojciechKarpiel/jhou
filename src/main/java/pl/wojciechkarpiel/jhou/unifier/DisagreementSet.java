package pl.wojciechkarpiel.jhou.unifier;


import pl.wojciechkarpiel.jhou.util.ListUtil;

import java.util.List;

public class DisagreementSet {

    private final List<DisagreementPair> disagreements;

    public DisagreementSet(List<DisagreementPair> disagreements) {
        this.disagreements = disagreements;
    }

    public static DisagreementSet of(DisagreementPair... disagreementPair) {
        return new DisagreementSet(ListUtil.of(disagreementPair));
    }

    public List<DisagreementPair> getDisagreements() {
        return disagreements;
    }
}
