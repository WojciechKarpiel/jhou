package pl.wojciechkarpiel.unifier.simplifier;

import pl.wojciechkarpiel.ast.Application;
import pl.wojciechkarpiel.ast.Constant;
import pl.wojciechkarpiel.ast.Term;
import pl.wojciechkarpiel.ast.Variable;
import pl.wojciechkarpiel.ast.type.ArrowType;
import pl.wojciechkarpiel.ast.type.Type;
import pl.wojciechkarpiel.ast.util.Id;
import pl.wojciechkarpiel.termHead.BetaEtaNormal;
import pl.wojciechkarpiel.termHead.Head;
import pl.wojciechkarpiel.termHead.HeadOps;
import pl.wojciechkarpiel.types.TypeCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


// TODO this is wrong arg count wrong see paper n binders, p,q args
// here did n args
public class Matcher {


    /**
     * @return possible solutions
     */
    public static List<BetaEtaNormal> match(BetaEtaNormal rigid, BetaEtaNormal flexible) {
        if (!rigid.isRigid()) throw new IllegalArgumentException();
        if (flexible.isRigid()) throw new IllegalArgumentException();
        List<BetaEtaNormal> res = new ArrayList<>();
        Optional<Constant> rigidConstant = HeadOps.asConstant(rigid.getHead());
        rigidConstant.ifPresent(c -> res.add(imitate(flexible, c, rigid.getArguments())));

        res.addAll(projections(flexible));

        return res;
    }

    public static List<BetaEtaNormal> projections(BetaEtaNormal flexible) {
        List<BetaEtaNormal> res = new ArrayList<>(flexible.getBinder().size());
        for (Variable bind : flexible.getBinder()) {
            res.add(projForBinder(bind, flexible.getBinder()));
        }

        return res;
    }

    private static BetaEtaNormal projForBinder(Variable bind, List<Variable> allBinders) {
        List<Term> args = new ArrayList<>(allBinders.size());

        Type resTpe = bind.getType();
        for (int i = allBinders.size() - 1; i >= 0; i--) {
            Variable lst = allBinders.get(i);
            resTpe = new ArrowType(lst.getType(), resTpe);
        }

        for (int i = 0; i < bind.getType().arity(); i++) {
            Term t = new Variable(Id.uniqueId(), resTpe);
            for (Variable hia : allBinders) {
                t = new Application(t, hia);
            }
            //sanity check
            if (!TypeCalculator.calculateType(t).equals(bind.getType())) throw new RuntimeException();
            args.add(t);
        }
        return new BetaEtaNormal(new Head.HeadVariable(bind), allBinders, args);
    }

    public static BetaEtaNormal imitate(BetaEtaNormal flexible, Constant c, List<Term> contantArgs) {
        List<Variable> binders = new ArrayList<>(flexible.getBinder());
        List<Term> newArgs = new ArrayList<>(contantArgs.size());


        for (Term cArg : contantArgs) {
            Type orig = TypeCalculator.calculateType(cArg);
            Type resTpe = orig;
            for (int i = binders.size() - 1; i >= 0; i--) {
                Variable lst = binders.get(i);
                resTpe = new ArrowType(lst.getType(), resTpe);
            }

            Term t = new Variable(Id.uniqueId(), resTpe);
            for (Variable binder : binders) {
                t = new Application(t, binder);
            }
            // sanity check
            if (!TypeCalculator.calculateType(t).equals(orig)) throw new RuntimeException();

            newArgs.add(t);
        }
        return new BetaEtaNormal(new Head.HeadConstant(c), binders, newArgs);
    }
}
