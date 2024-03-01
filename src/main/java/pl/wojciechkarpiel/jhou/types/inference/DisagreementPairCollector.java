package pl.wojciechkarpiel.jhou.types.inference;

import pl.wojciechkarpiel.jhou.ast.Application;
import pl.wojciechkarpiel.jhou.ast.Constant;
import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.ast.Variable;
import pl.wojciechkarpiel.jhou.ast.type.ArrowType;
import pl.wojciechkarpiel.jhou.ast.type.BaseType;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.ast.type.TypeVisitor;
import pl.wojciechkarpiel.jhou.ast.util.Id;
import pl.wojciechkarpiel.jhou.types.TypeCalculator;
import pl.wojciechkarpiel.jhou.unifier.DisagreementPair;
import pl.wojciechkarpiel.jhou.unifier.DisagreementSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pl.wojciechkarpiel.jhou.types.inference.TypeInference.ARROW;
import static pl.wojciechkarpiel.jhou.types.inference.TypeInference.DUMMY_TYPE;

class DisagreementPairCollector {

     private final Annotations annotations;

     DisagreementPairCollector(Annotations annotations){
         this.annotations = annotations;
     }

     Map<Constant,Type> constantTypeMap(){
         HashMap<Constant, Type> constantTypeMap = new HashMap<>(typeConstantMap.size());
         for (Map.Entry<Type, Constant> typeConstantEntry : typeConstantMap.entrySet()) {
             constantTypeMap.put(typeConstantEntry.getValue(),typeConstantEntry.getKey());
         }
        return constantTypeMap;
     }

     DisagreementSet collect(){
         final List<DisagreementPair> dps = new ArrayList<>();

         for (AnnotatedTerm annotation : annotations.getAnnotations()) {
             Term origTerm = annotation.originalTerm;
             Term fakeVar = annotation.annotation;
             if (origTerm instanceof Application) {
                 Application app = (Application) origTerm;

                 Term anon = annotations.getAnnotation(app.getFunction());
                 Application b = new Application(
                         new Application(ARROW,
                                 annotations.getAnnotation(app.getArgument())),
                         fakeVar);
                 TypeCalculator.ensureEqualTypes(anon, b);
                 DisagreementPair e = new DisagreementPair(
                         anon,
                         b

                 );
                 dps.add(e);

             }
             {
                 Type rlType = null;
                 if (origTerm instanceof Constant) {
                     rlType = ((Constant) origTerm).getType();
                 } else if (origTerm instanceof Variable) {
                     rlType = ((Variable) origTerm).getType();
                 }
                 if (rlType == null) continue;

                 Term target = ftchType(rlType);

                 TypeCalculator.ensureEqualTypes(target, fakeVar);
                 DisagreementPair e = new DisagreementPair(
                         target,
                         fakeVar

                 );
                 dps.add(e);
             }
         }
         return new DisagreementSet(dps);
     }

    private final Map<Type, Constant> typeConstantMap = new HashMap<>();


    private Term ftchType(Type rlType) {
        return rlType.visit(new TypeVisitor<Term>() {
            @Override
            public Term visitBaseType(BaseType baseType) {
                Constant tm;
                if (typeConstantMap.containsKey(baseType)) {
                    tm = typeConstantMap.get(baseType);
                } else {
                    tm = new Constant(Id.uniqueId(), DUMMY_TYPE);
                    typeConstantMap.put(baseType, tm);
                }
                return tm;
            }

            @Override
            public Term visitArrowType(ArrowType arrowType) {
                Term fr = ftchType(arrowType.getFrom());
                Term to = ftchType(arrowType.getTo());
                return Application.apply(ARROW, fr, to);
            }
        });
    }
 }
