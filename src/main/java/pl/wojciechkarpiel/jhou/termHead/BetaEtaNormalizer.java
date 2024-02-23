package pl.wojciechkarpiel.jhou.termHead;

import pl.wojciechkarpiel.jhou.ast.*;
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


    static Term etaExpand(Term t, int times) {
        for (int i = 0; i < times; i++) {
            t = Normalizer.etaExpand(t);
        }
        return t;
    }


    static BetaEtaNormal normalize(Term term, List<Variable> outsideBinders) {
        BetaEtaNormalizer b = new BetaEtaNormalizer();
        b.binder.addAll(outsideBinders);
        final Term normalized = Normalizer.betaNormalize(term);
        Head h = b.normalizeInt(normalized);

        Collections.reverse(b.arguments); // were collected in the brackwards order

        ////// ETA EXPANSION
        int n = b.binder.size();
        Type termType = TypeCalculator.calculateType(normalized);
        int m = termType.arity();

        int etaExpansionNums = Math.max(0, m - n);

        if (etaExpansionNums > 0) {
            Term eted = injectInnerEta(normalized, etaExpansionNums);
            return normalize(eted, outsideBinders);
        }

        return new BetaEtaNormal(h, b.binder, b.arguments);
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


    private static Term injectInnerEta(Term term, int times) {
        return term.visit(new Visitor<Term>() {
            @Override
            public Term visitConstant(Constant constant) {
                return etaExpand(constant, times);
            }

            @Override
            public Term visitVariable(Variable variable) {
                return etaExpand(variable, times);
            }

            @Override
            public Term visitApplication(Application application) {
                return etaExpand(application, times);
            }

            @Override
            public Term visitAbstraction(Abstraction abstraction) {
                return new Abstraction(abstraction.getVariable(), injectInnerEta(abstraction.getBody(), times));
            }
        });
    }

}
