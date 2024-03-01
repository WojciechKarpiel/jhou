package pl.wojciechkarpiel.jhou.types.inference;

import pl.wojciechkarpiel.jhou.ast.*;
import pl.wojciechkarpiel.jhou.ast.type.ArrowType;
import pl.wojciechkarpiel.jhou.ast.type.BaseType;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.ast.util.Id;
import pl.wojciechkarpiel.jhou.ast.util.Visitor;
import pl.wojciechkarpiel.jhou.substitution.Substitution;
import pl.wojciechkarpiel.jhou.unifier.AllowedTypeInference;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static pl.wojciechkarpiel.jhou.types.inference.TypeInference.ARROW;

class TypeRebuilder {

    private final Substitution substitution;
    private final Annotations annotations;
    private final Map<Constant, Type> constantTypeMap;
    private final PrintStream printStream;
    private final AllowedTypeInference allowedTypeInference;

    TypeRebuilder(Substitution substitution, Annotations annotations, Map<Constant,Type> constantTypeMap,
                  PrintStream printStream, AllowedTypeInference allowedTypeInference){
        this.substitution = substitution;
        this.annotations = annotations;
        this.constantTypeMap = constantTypeMap;
        this.printStream = printStream;
        this.allowedTypeInference = allowedTypeInference;
    }


    Term rebuild(Term input){
        return input.visit(new Visitor<Term>() {
            @Override
            public Term visitConstant(Constant constant) {
                return new Constant(constant.getId(), getT(constant), constant.toString()); //todo preserve name better
            }

            @Override
            public Term visitVariable(Variable variable) {
                return new Variable(variable.getId(), getT(variable), variable.toString()); //todo preserve name better;
            }

            @Override
            public Term visitApplication(Application application) {
                return new Application(
                        rebuild(application.getFunction()),
                        rebuild(application.getArgument())
                );
            }

            @Override
            public Term visitAbstraction(Abstraction abstraction) {
                return new Abstraction(
                        (Variable) rebuild(abstraction.getVariable()),
                        rebuild(abstraction.getBody())
                );
            }
        });
    }

    private Type getT(Term t) {
        Type tt;
        Term b = null;

        if (t instanceof Variable) {
            b = annotations.getAnnotation(t);
        }
        if (t instanceof Constant) {
            b = annotations.getAnnotation(t);
        }
        if (b != null) {
            Term q = substitution.substitute(b);
            tt = fakeVarToRealType(q);
            return tt;
        }
        throw new RuntimeException();
    }


    private Type fakeVarToRealType(Term t) {
        return t.visit(new Visitor<Type>() {
            @Override
            public Type visitConstant(Constant constant) {
                Type type = constantTypeMap.get(constant);
                if (type == null) {
                    if (AllowedTypeInference.PERMISSIVE != allowedTypeInference) {
                        throw new TypeInference.InferenceHasArbitrarySolutionsException();

                    }
                    Id id = Id.uniqueId();
                    BaseType freshType = new BaseType(id, "infered_arbitrarty_" + id.getId());
                    printStream.println("Creating a new, arbitrary type: " + freshType);
                    getNewTypes().add(freshType);
                    constantTypeMap.put(constant, freshType);
                    return fakeVarToRealType(constant);
                }
                return type;
            }

            @Override
            public Type visitVariable(Variable variable) {
                throw new RuntimeException();
            }

            @Override
            public Type visitApplication(Application application_) {
                Application application = (Application) application_.getFunction();
                if (application.getFunction() != ARROW) throw new RuntimeException();

                Type from = fakeVarToRealType(application.getArgument());
                Type to = fakeVarToRealType(application_.getArgument());
                return new ArrowType(from, to);
            }


            @Override
            public Type visitAbstraction(Abstraction abstraction) {
                throw new RuntimeException();
            }
        });
    }


    private Set<Type> newTypes = null;

    public final Set<Type> getNewTypes() {
        if (newTypes == null) newTypes = new HashSet<>();
        return newTypes;
    }
}
