package pl.wojciechkarpiel.jhou.alpha;

import pl.wojciechkarpiel.jhou.ast.Abstraction;
import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.ast.Variable;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.substitution.Substitution;
import pl.wojciechkarpiel.jhou.substitution.SubstitutionPair;
import pl.wojciechkarpiel.jhou.termHead.BetaEtaNormal;
import pl.wojciechkarpiel.jhou.termHead.Head;
import pl.wojciechkarpiel.jhou.util.ListUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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


    public static boolean headAlphaUnifiable(Term a, Term b) {
        return alphaEqualizeHeading(BetaEtaNormal.normalize(a), BetaEtaNormal.normalize(b)).isPresent();
    }

    public static Optional<BenPair> alphaEqualizeHeading(BetaEtaNormal left, BetaEtaNormal right) {
        if (!equalHeadings(left, right)) return Optional.empty();
        if (left.getArguments().size() != right.getArguments().size()) return Optional.empty();

        int bindersSize = left.getBinder().size();
        List<Variable> newBinders = new ArrayList<>(bindersSize);

        // snaity check, duplicated variables shouldn't be possible to construct
        {
            int a = new HashSet<>(left.getBinder()).size();
            int b = new HashSet<>(right.getBinder()).size();
            int c = right.getBinder().size();
            int d = left.getBinder().size();
            if (!(a == b && b == c && c == d)) {
                throw new RuntimeException();
            }
        }
        List<SubstitutionPair> leftSub = new ArrayList<>(bindersSize);
        List<SubstitutionPair> rightSub = new ArrayList<>(bindersSize);
        for (int i = 0; i < bindersSize; i++) {

            Type type = left.getBinder().get(i).getType();
            Type type2 = right.getBinder().get(i).getType();
            if (!type.equals(type2)) throw new RuntimeException();
            Variable v = Variable.freshVariable(type);
            newBinders.add(v);
            leftSub.add(new SubstitutionPair(left.getBinder().get(i), v));
            rightSub.add(new SubstitutionPair(right.getBinder().get(i), v));
        }
        Substitution leftS = new Substitution(leftSub);
        Substitution rightS = new Substitution(rightSub);

        BetaEtaNormal l = AlphaEqual.substitute(leftS, newBinders, left);
        BetaEtaNormal r = AlphaEqual.substitute(rightS, newBinders, right);
        return Optional.of(new BenPair(l, r));
    }

    private static BetaEtaNormal substitute(Substitution s, List<Variable> newBinders, BetaEtaNormal ben) {
        return BetaEtaNormal.fromFakeNormal(
                Head.fromTerm(s.substitute(ben.getHead().getTerm())),
                newBinders,
                ben.getArguments().stream().map(s::substitute).collect(Collectors.toList())
        );
    }

    private static boolean equalHeadings(BetaEtaNormal a, BetaEtaNormal b) {
        BetaEtaNormal aLol = BetaEtaNormal.fromFakeNormal(a.getHead(), a.getBinder(), ListUtil.of());
        BetaEtaNormal bLol = BetaEtaNormal.fromFakeNormal(b.getHead(), b.getBinder(), ListUtil.of());
        return aLol.backToTerm().equals(bLol.backToTerm());
    }

    public static class BenPair {
        private final BetaEtaNormal left;
        private final BetaEtaNormal right;

        public BenPair(BetaEtaNormal left, BetaEtaNormal right) {
            this.left = left;
            this.right = right;
        }

        public BetaEtaNormal getLeft() {
            return left;
        }

        public BetaEtaNormal getRight() {
            return right;
        }
    }
}
