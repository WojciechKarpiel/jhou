package pl.wojciechkarpiel.jhou.termHead;

import pl.wojciechkarpiel.jhou.ast.*;
import pl.wojciechkarpiel.jhou.ast.util.Visitor;
import pl.wojciechkarpiel.jhou.normalizer.Normalizer;

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
        Term normalized = Normalizer.betaNormalize(term);
        Head h = b.normalizeInt(normalized);
        Collections.reverse(b.arguments); // were collected in the brackwards order
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


}