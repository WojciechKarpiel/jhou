package pl.wojciechkarpiel.jhou.unifier;


import java.util.List;

// TODO: use fancy VLists?
public class DisagreementSet {

    private final List<DisagreementPair> disagreements;

    // lol not a set
    public DisagreementSet(List<DisagreementPair> disagreements) {
        this.disagreements = disagreements;
    }

    public List<DisagreementPair> getDisagreements() {
        return disagreements;
    }
}
