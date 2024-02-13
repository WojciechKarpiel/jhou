package pl.wojciechkarpiel.termHead;

import pl.wojciechkarpiel.ast.*;
import pl.wojciechkarpiel.ast.util.Visitor;
import pl.wojciechkarpiel.substitution.Substitution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HeaderUnifier {

    public static boolean headAlphaUnifiable(Term t1, Term t2) {
        return alphaUnifyHeaderReturnNewRight(BetaEtaNormal.normalize(t1), BetaEtaNormal.normalize(t2)).isPresent();
    }

    // assume both rigid and variable based
    public static Optional<BetaEtaNormal> alphaUnifyHeaderReturnNewRight(BetaEtaNormal left, BetaEtaNormal right) {
        List<Variable> newRightBinders = new ArrayList<>(left.getBinder());
        List<Term> newRightArgs = new ArrayList<>(right.getArguments());
        Term newRightHead = right.getHead().getTerm();

        for (int i = right.getBinder().size() - 1; i >= 0; i--) {
            Variable rightB = right.getBinder().get(i);
            Variable leftB = left.getBinder().get(i);
            if (rightB.equals(leftB)) continue;
            Substitution s = new Substitution(rightB, leftB);
            newRightHead = s.substitute(newRightHead);
            List<Term> newNewArgs = new ArrayList<>(right.getArguments().size());
            newRightArgs.forEach(arg -> newNewArgs.add(s.substitute(arg)));
            newRightArgs = newNewArgs;
        }

        if (newRightHead.equals(left.getHead().getTerm()) &&
                newRightBinders.equals(left.getBinder())
        ) {
            Head hd = newRightHead.visit(new Visitor<Head>() {
                @Override
                public Head visitConstant(Constant constant) {
                    return new Head.HeadConstant(constant);
                }

                @Override
                public Head visitVariable(Variable variable) {
                    return new Head.HeadVariable(variable);
                }

                @Override
                public Head visitApplication(Application application) {
                    throw new RuntimeException();
                }

                @Override
                public Head visitAbstraction(Abstraction abstraction) {
                    throw new RuntimeException();
                }
            });
            return Optional.of(new BetaEtaNormal(hd, newRightBinders, newRightArgs));
        } else return Optional.empty();
    }
}
