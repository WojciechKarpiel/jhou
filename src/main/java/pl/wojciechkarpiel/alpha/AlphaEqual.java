package pl.wojciechkarpiel.alpha;

import pl.wojciechkarpiel.ast.Abstraction;
import pl.wojciechkarpiel.ast.Term;
import pl.wojciechkarpiel.ast.Variable;
import pl.wojciechkarpiel.substitution.Substitution;

public class AlphaEqual {

    private AlphaEqual() {
    }

    public static boolean isAlphaEqual(Abstraction a, Abstraction b) {
        if (a.getVariable().equals(b.getVariable())) return a.getBody().equals(b.getBody());
        if (!a.getVariable().getType().equals(b.getVariable().getType())) return false;
        Variable v = Variable.freshVariable(a.getVariable().getType());
        Substitution aSub = new Substitution(a.getVariable(), v);
        Substitution bSub = new Substitution(b.getVariable(), v);
        Term aVBody = aSub.substitute(a.getBody());
        Term bVBody = bSub.substitute(b.getBody());
        return aVBody.equals(bVBody);
    }
}
