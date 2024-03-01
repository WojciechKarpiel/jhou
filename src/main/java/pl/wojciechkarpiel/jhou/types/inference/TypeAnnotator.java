package pl.wojciechkarpiel.jhou.types.inference;

import pl.wojciechkarpiel.jhou.ast.*;
import pl.wojciechkarpiel.jhou.ast.util.Visitor;
import pl.wojciechkarpiel.jhou.util.MapUtil;

import java.util.HashMap;
import java.util.List;

import static pl.wojciechkarpiel.jhou.types.inference.TypeInference.ARROW;
import static pl.wojciechkarpiel.jhou.types.inference.TypeInference.DUMMY_TYPE;

class TypeAnnotator {


    static Annotations annotate(List<Term> terms) {
        TypeAnnotator annotator = new TypeAnnotator();
        for (Term term : terms) {
            annotator.annotateInternal(term);
        }
        return annotator.annotations;
    }

    private TypeAnnotator() {
    }

    private final Annotations annotations = new Annotations();
    private final MapUtil<Variable, Variable> varCache = new MapUtil<>(new HashMap<>());
    private final MapUtil<Constant, Variable> conCache = new MapUtil<>(new HashMap<>());

    private final MapUtil<Variable, Variable> bound = new MapUtil<>(new HashMap<>());


    private void annotateInternal(Term t) {
        t.visit(new Visitor<Void>() {
            @Override
            public Void visitConstant(Constant constant) {
                Variable rt;
                if (conCache.get(constant).isPresent()) {
                    rt = conCache.get(constant).get();
                } else {
                    rt = Variable.freshVariable(DUMMY_TYPE);
                    conCache.put(constant, rt);
                }
                annotations.addAnnotation(constant, rt);
                return null;
            }

            @Override
            public Void visitVariable(Variable variable) {
                Variable rt;
                if (bound.get(variable).isPresent()) {
                    rt = bound.get(variable).get();
                } else if (varCache.get(variable).isPresent()) {
                    rt = varCache.get(variable).get();
                } else {
                    rt = Variable.freshVariable(DUMMY_TYPE);
                    varCache.put(variable, rt);
                }
                annotations.addAnnotation(variable, rt);
                return null;
            }

            @Override
            public Void visitApplication(Application application) {
                annotations.addAnnotation(application, Variable.freshVariable(DUMMY_TYPE));
                annotateInternal(application.getArgument());
                annotateInternal(application.getFunction());
                return null;
            }

            @Override
            public Void visitAbstraction(Abstraction abstraction) {
                Variable nv = Variable.freshVariable(DUMMY_TYPE);
                annotations.addAnnotation(abstraction.getVariable(), nv);
                bound.withMapping(abstraction.getVariable(), nv,
                        () -> {
                            annotateInternal(abstraction.getBody());
                            return null;
                        });
                annotations.addAnnotation(abstraction,
                        new Application(
                                new Application(ARROW, nv),
                                annotations.getAnnotation(abstraction.getBody()))
                );
                return null;
            }
        });
    }
}
