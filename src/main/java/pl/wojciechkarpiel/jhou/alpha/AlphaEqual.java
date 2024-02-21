package pl.wojciechkarpiel.jhou.alpha;

import pl.wojciechkarpiel.jhou.ast.*;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.ast.util.Visitor;
import pl.wojciechkarpiel.jhou.substitution.Substitution;
import pl.wojciechkarpiel.jhou.substitution.SubstitutionPair;
import pl.wojciechkarpiel.jhou.termHead.BetaEtaNormal;
import pl.wojciechkarpiel.jhou.termHead.Head;
import pl.wojciechkarpiel.jhou.util.ListUtil;
import pl.wojciechkarpiel.jhou.util.MapUtil;
import pl.wojciechkarpiel.jhou.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class AlphaEqual {

    private AlphaEqual() {
    }

    public static boolean isAlphaEqual(Abstraction a, Abstraction b) {
        return LazyAlphaEqual.isAlphaEqualLazy(a, b);
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

    public static class BenPair extends Pair<BetaEtaNormal, BetaEtaNormal> {
        public BenPair(BetaEtaNormal left, BetaEtaNormal right) {
            super(left, right);
        }
    }

    private static class LazyAlphaEqual {

        static boolean isAlphaEqualLazy(Term a, Term b) {
            return new LazyAlphaEqual().alphaEqual(a, b);
        }

        private LazyAlphaEqual() {

        }

        private final MapUtil<Variable, Variable> leftSub = new MapUtil<>(new HashMap<>());
        private final MapUtil<Variable, Variable> rightSub = new MapUtil<>(new HashMap<>());

        private boolean alphaEqual(Term left, Term right) {
            return left.visit(new Visitor<Boolean>() {
                @Override
                public Boolean visitConstant(Constant constant) {
                    return constant.equals(right);
                }

                @Override
                public Boolean visitVariable(Variable variable) {
                    Variable subL = leftSub.get(variable).orElse(variable);
                    if (right instanceof Variable) {
                        Variable subR = rightSub.get((Variable) right).orElse((Variable) right);
                        return subL.equals(subR);
                    } else return false;
                }

                @Override
                public Boolean visitApplication(Application application) {
                    if (right instanceof Application) {
                        Application r = (Application) right;
                        return alphaEqual(application.getFunction(), r.getFunction()) &&
                                alphaEqual(application.getArgument(), r.getArgument());
                    } else return false;
                }

                @Override
                public Boolean visitAbstraction(Abstraction leftAbs) {
                    if (right instanceof Abstraction) {
                        Abstraction rightAbs = (Abstraction) right;
                        if (rightAbs.getVariable().equals(leftAbs.getVariable()))
                            return alphaEqual(leftAbs.getBody(), rightAbs.getBody());
                        if (!rightAbs.getVariable().getType().equals(leftAbs.getVariable().getType())) return false;
                        Variable newVar = Variable.freshVariable(rightAbs.getVariable().getType());
                        return leftSub.withMapping(leftAbs.getVariable(), newVar,
                                () -> rightSub.withMapping(rightAbs.getVariable(), newVar,
                                        () -> alphaEqual(leftAbs.getBody(), rightAbs.getBody())));
                    } else return false;
                }
            });
        }
    }
}
