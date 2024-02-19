package pl.wojciechkarpiel.jhou.types.inference;

import pl.wojciechkarpiel.jhou.ast.*;
import pl.wojciechkarpiel.jhou.ast.type.ArrowType;
import pl.wojciechkarpiel.jhou.ast.type.BaseType;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.ast.type.TypeVisitor;
import pl.wojciechkarpiel.jhou.ast.util.Id;
import pl.wojciechkarpiel.jhou.ast.util.Visitor;
import pl.wojciechkarpiel.jhou.substitution.Substitution;
import pl.wojciechkarpiel.jhou.types.TypeCalculator;
import pl.wojciechkarpiel.jhou.unifier.AllowedTypeInference;
import pl.wojciechkarpiel.jhou.unifier.DisagreementPair;
import pl.wojciechkarpiel.jhou.unifier.DisagreementSet;
import pl.wojciechkarpiel.jhou.unifier.SolutionIterator;
import pl.wojciechkarpiel.jhou.unifier.tree.Tree;
import pl.wojciechkarpiel.jhou.unifier.tree.WorkWorkNode;
import pl.wojciechkarpiel.jhou.util.DevNullPrintStream;
import pl.wojciechkarpiel.jhou.util.ListUtil;
import pl.wojciechkarpiel.jhou.util.MapUtil;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO: this entire class is hideous
public class TypeInference {

    public static Term inferMissing(Term term) {
        return inferMissing(ListUtil.of(term)).get(0);
    }

    public static List<Term> inferMissing(List<Term> termsOfEqualType) {
        return inferMissing(termsOfEqualType, System.out);
    }

    public static List<Term> inferMissing(List<Term> termsOfEqualType, PrintStream printStream) {
        return inferMissing(termsOfEqualType, AllowedTypeInference.PERMISSIVE, printStream);
    }

    public static List<Term> inferMissing(List<Term> termsOfSameType, AllowedTypeInference allowedTypeInference) {
        return inferMissing(termsOfSameType, allowedTypeInference, System.out);
    }

    public static List<Term> inferMissing(List<Term> termsOfEqualType, AllowedTypeInference allowedTypeInference, PrintStream printStream) {

        if (termsOfEqualType.stream().noneMatch(TypeInference::needsInference)) {
            printStream.println("No need for type inference, types fully instantiated");
            return termsOfEqualType;
        }
        if (allowedTypeInference == AllowedTypeInference.NO_INFERENCE_ALLOWED) {
            throw new InferenceRequiredButNotAllowedException();
        }


        TypeInference l3l = new TypeInference(printStream, allowedTypeInference);

        for (Term term : termsOfEqualType) {
            l3l.annotate(term);
        }
        List<DisagreementPair> ds = l3l.collectDps();
        // + toplevel reuirements:
        for (int i = 0; i < termsOfEqualType.size() - 1; i++) {
            Term prev = termsOfEqualType.get(i);
            Term next = termsOfEqualType.get(i + 1);
            Term anon = l3l.getAnon(prev);
            Term anon1 = l3l.getAnon(next);
            ds.add(
                    new DisagreementPair(
                            anon,
                            anon1
                    )
            );
        }

        DisagreementSet disagreementSet = new DisagreementSet(ds);

        boolean previousValueToRestore = WorkWorkNode.PRETEND_YOU_RE_DOING_FIRST_ORDER_UNIFICATION;
        WorkWorkNode.PRETEND_YOU_RE_DOING_FIRST_ORDER_UNIFICATION = true;
        Tree tree = new WorkWorkNode(null, Substitution.empty(), disagreementSet);
        SolutionIterator s = new SolutionIterator(tree, DevNullPrintStream.INSTANCE);
        Substitution nxt = s.next();
        WorkWorkNode.PRETEND_YOU_RE_DOING_FIRST_ORDER_UNIFICATION = previousValueToRestore;
        return termsOfEqualType.stream().map(term -> l3l.recreateWithTypes(term, nxt)).collect(Collectors.toList());
    }


    public static boolean needsInference(Term term) {
        return term.visit(new Visitor<Boolean>() {
            @Override
            public Boolean visitConstant(Constant constant) {
                return constant.getType() == null;
            }

            @Override
            public Boolean visitVariable(Variable variable) {
                return variable.getType() == null;
            }

            @Override
            public Boolean visitApplication(Application application) {
                return needsInference(application.getFunction()) || needsInference(application.getArgument());
            }

            @Override
            public Boolean visitAbstraction(Abstraction abstraction) {
                return needsInference(abstraction.getVariable()) || needsInference(abstraction.getBody());
            }
        });
    }


    private final PrintStream printStream;

    private TypeInference(PrintStream printStream, AllowedTypeInference allowedTypeInference) {
        this.printStream = printStream;
        this.allowedInference = allowedTypeInference;
    }

    private boolean inventedNewTypes = false;


    private Type getT(Term t, Substitution s) {
        Type tt = null;
        Term b = null;

        Term a = t;
        if (a instanceof Variable) {
            b = getAnon(a);
        }
        if (a instanceof Constant) {
            b = getAnon(a);
        }
        if (b != null) {
            Term q = s.substitute(b);
            tt = fakeVarToRealType(q);
            return tt;
        }
        throw new RuntimeException();
    }

    private Term recreateWithTypes(Term input, Substitution s) {
        return input.visit(new Visitor<Term>() {
            @Override
            public Term visitConstant(Constant constant) {
                return new Constant(constant.getId(), getT(constant, s), constant.toString()); //todo preserve name better
            }

            @Override
            public Term visitVariable(Variable variable) {
                return new Variable(variable.getId(), getT(variable, s), variable.toString()); //todo preserve name better;
            }

            @Override
            public Term visitApplication(Application application) {
                return new Application(
                        recreateWithTypes(application.getFunction(), s),
                        recreateWithTypes(application.getArgument(), s)
                );
            }

            @Override
            public Term visitAbstraction(Abstraction abstraction) {
                return new Abstraction(
                        (Variable) recreateWithTypes(abstraction.getVariable(), s),
                        recreateWithTypes(abstraction.getBody(), s)
                );
            }
        });
    }

    public static class InferenceRequiredButNotAllowedException extends RuntimeException {
    }

    public static class InferenceHasArbitrarySolutionsException extends RuntimeException {
    }

    AllowedTypeInference allowedInference = AllowedTypeInference.PERMISSIVE;

    private Type fakeVarToRealType(Term t) {
        return t.visit(new Visitor<Type>() {
            @Override
            public Type visitConstant(Constant constant) {
                Type type = constanTypeMap.get(constant);
                if (type == null) {
                    inventedNewTypes = true;
                    if (AllowedTypeInference.PERMISSIVE != allowedInference) {
                        throw new InferenceHasArbitrarySolutionsException();

                    }
                    // TODO throw un the future
                    Id id = Id.uniqueId();
                    BaseType value = new BaseType(id, "infered_arbitrarty_" + id.getId());
                    printStream.println("Creating a new, arbitrary type: " + value);
                    constanTypeMap.put(constant, value);
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

    private final static Type DUMMY_TYPE = BaseType.freshBaseType("dummy_type");
    private final static Constant ARROW = Constant.freshConstant(new ArrowType(DUMMY_TYPE, new ArrowType(DUMMY_TYPE, DUMMY_TYPE)), "ARR");


    private final Map<Type, Constant> typeConstantMap = new HashMap<>();
    private final Map<Constant, Type> constanTypeMap = new HashMap<>();


    private List<DisagreementPair> collectDps() {
        final List<DisagreementPair> dps = new ArrayList<>();

        for (TermPair annotation : annotations) {
            Term origTerm = annotation.a;
            Term fakeVar = annotation.b;
            if (origTerm instanceof Application) {
                Application app = (Application) origTerm;

                Term anon = getAnon(app.getFunction());
                Application b = new Application(
                        new Application(ARROW,
                                getAnon(app.getArgument())),
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
                Term orig = origTerm;
                if (orig instanceof Constant) {
                    rlType = ((Constant) orig).getType();
                } else if (orig instanceof Variable) {
                    rlType = ((Variable) orig).getType();
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
        return dps;
    }

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
                constanTypeMap.put(tm, baseType);
                return tm;
            }

            @Override
            public Term visitArrowType(ArrowType arrowType) {
                Term fr = ftchType(arrowType.getFrom());
                Term to = ftchType(arrowType.getTo());
                return new Application(new Application(ARROW, fr), to);
            }
        });
    }


    private static class TermPair {
        private final Term a;
        private final Term b;

        private TermPair(Term a, Term b) {
            this.a = a;
            this.b = b;
        }
    }

    private final List<TermPair> annotations = new ArrayList<>();

    private void addAnon(Term a, Term b) {
        annotations.add(new TermPair(a, b));
    }

    private Term getAnon(Term a) {
        for (TermPair annotation : annotations) {
            if (annotation.a == a) { // delibereately by ref
                return annotation.b;
            }
        }
        throw new RuntimeException();
    }

    private final MapUtil<Variable, Variable> varCache = new MapUtil<>(new HashMap<>());
    private final MapUtil<Constant, Variable> conCache = new MapUtil<>(new HashMap<>());

    private final MapUtil<Variable, Variable> bound = new MapUtil<>(new HashMap<>());


    private void annotate(Term t) {
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
                addAnon(constant, rt);
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
                addAnon(variable, rt);
                return null;
            }

            @Override
            public Void visitApplication(Application application) {
                addAnon(application, Variable.freshVariable(DUMMY_TYPE));
                annotate(application.getArgument());
                annotate(application.getFunction());
                return null;
            }

            @Override
            public Void visitAbstraction(Abstraction abstraction) {
                Variable nv = Variable.freshVariable(DUMMY_TYPE);
                addAnon(abstraction.getVariable(), nv);
                bound.withMapping(abstraction.getVariable(), nv,
                        () -> {
                            annotate(abstraction.getBody());
                            return null;
                        });
                addAnon(abstraction,
                        new Application(
                                new Application(ARROW, nv),
                                getAnon(abstraction.getBody()))
                );
                return null;
            }
        });
    }


}
