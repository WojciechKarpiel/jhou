package pl.wojciechkarpiel.jhou.ast.util;

import pl.wojciechkarpiel.jhou.ast.*;
import pl.wojciechkarpiel.jhou.util.MapUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class FreeVariable {
    private FreeVariable() {
    }

    public static Set<Variable> getFreeVariables(Term term) {
        FreeVariableCollector collector = new FreeVariableCollector();
        collector.collect(term);
        return collector.freeVariables;
    }


    private static class FreeVariableCollector {
        final Set<Variable> freeVariables = new HashSet<>();
        final MapUtil<Variable, Unit> boundVariables = new MapUtil<>(new HashMap<>());

        Void collect(Term t) {
            return t.visit(new Visitor<Void>() {
                @Override
                public Void visitConstant(Constant constant) {
                    return null;
                }

                @Override
                public Void visitVariable(Variable variable) {
                    if (boundVariables.get(variable).isPresent()) return null;
                    freeVariables.add(variable);
                    return null;
                }

                @Override
                public Void visitApplication(Application application) {
                    collect(application.getFunction());
                    collect(application.getArgument());
                    return null;
                }

                @Override
                public Void visitAbstraction(Abstraction abstraction) {
                    boundVariables.withMapping(abstraction.getVariable(),
                            Unit.INSTANCE,
                            () -> collect(abstraction.getBody()));
                    return null;
                }
            });
        }
    }

    private enum Unit {
        INSTANCE
    }
}
