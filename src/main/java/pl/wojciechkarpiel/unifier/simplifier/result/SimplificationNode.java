package pl.wojciechkarpiel.unifier.simplifier.result;

import pl.wojciechkarpiel.substitution.Substitution;
import pl.wojciechkarpiel.unifier.DisagreementSet;

import java.util.HashMap;

public class SimplificationNode implements SimplificationResult {

    private final DisagreementSet disagreement;

    public static SimplificationResult fromDisagreements(DisagreementSet disagreement) {
        if (disagreement.getDisagreements().isEmpty())
            return new SimplificationSuccess(new Substitution(new HashMap<>()));
        else return new SimplificationNode(disagreement);
    }

    public SimplificationNode(DisagreementSet disagreement) {
        this.disagreement = disagreement;
    }

    public DisagreementSet getDisagreement() {
        return disagreement;
    }

    @Override
    public boolean isFailure() {
        return false;
    }

    @Override
    public <T> T visit(SimplificationVisitor<T> visitor) {
        return visitor.visitNode(this);
    }
}
