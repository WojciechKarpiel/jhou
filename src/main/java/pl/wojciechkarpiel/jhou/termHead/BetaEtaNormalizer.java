package pl.wojciechkarpiel.jhou.termHead;

import pl.wojciechkarpiel.jhou.ast.*;
import pl.wojciechkarpiel.jhou.ast.type.ArrowType;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.ast.util.Visitor;
import pl.wojciechkarpiel.jhou.normalizer.Normalizer;
import pl.wojciechkarpiel.jhou.types.TypeCalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class BetaEtaNormalizer {

    static BetaEtaNormal normalize(Term term) {
        return BetaEtaNormalizer.normalize(term, new ArrayList<Variable>());
    }

    static BetaEtaNormal normalize(Term term, List<Variable> outsideBinders) {
        BetaEtaNormalizer b = new BetaEtaNormalizer();
        b.binder.addAll(outsideBinders);
        final Term normalized = Normalizer.betaNormalize(term);
        Head h = b.normalizeInt(normalized);


        ////// ETA EXPANSION
        int n = b.binder.size();
        Type termType = TypeCalculator.calculateType(normalized);
        int m = termType.arity();

        int etaExpansionNums = Math.max(0, m - n);

        Type tt = TypeCalculator.calculateType(h.getTerm());
        List<Variable> additionalBinders = new ArrayList<>(etaExpansionNums);
        for (int i = 0; i < etaExpansionNums; i++) {
            ArrowType at = ((ArrowType) tt);
            Variable newBinder = Variable.freshVariable(at.getFrom());
            additionalBinders.add(newBinder);
            tt = at.getTo();
        }


        List<Term> finalArgs = new ArrayList<>(b.arguments.size() + additionalBinders.size());
        finalArgs.addAll(additionalBinders);
        Collections.reverse(b.arguments); // were collected in the brackwards order
        finalArgs.addAll(b.arguments);

//        Collections.reverse(additionalBinders);
        b.binder.addAll(additionalBinders);

        BetaEtaNormal result = new BetaEtaNormal(h, b.binder, finalArgs);

        {
            // sanity check
            List<Variable> clearBinders = new ArrayList<>();
            for (int i = 0; i < result.getBinder().size(); i++) {
                if (i < outsideBinders.size()) continue;
                clearBinders.add(result.getBinder().get(i));
            }
            BetaEtaNormal rr = new BetaEtaNormal(result.getHead(), clearBinders, result.getArguments());
            Term tttt = rr.backToTerm();
            if (!Normalizer.etaNormalize(tttt).equals(Normalizer.etaNormalize(normalized))) {
                throw new RuntimeException();
            }
        }

        return result;
    }

    private final List<Variable> binder = new ArrayList<>();
    private final List<Term> arguments = new ArrayList<>();

//    private final MapUtil<Variable, Unit> boundVariables = new MapUtil<>(new HashMap<>());

    private Head normalizeInt(Term term) {
        return term.visit(new Visitor<Head>() {
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
                Term arg = application.getArgument();
                arguments.add(arg);
                return normalizeInt(application.getFunction());
            }

            @Override
            public Head visitAbstraction(Abstraction abstraction) {
                Variable v = abstraction.getVariable();
                binder.add(v);
//                    boundVariables.put(v,Unit.UNIT);
                return normalizeInt(abstraction.getBody());
            }
        });
    }


}
