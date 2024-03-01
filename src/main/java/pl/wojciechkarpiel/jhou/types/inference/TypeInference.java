package pl.wojciechkarpiel.jhou.types.inference;

import pl.wojciechkarpiel.jhou.ast.*;
import pl.wojciechkarpiel.jhou.ast.type.ArrowType;
import pl.wojciechkarpiel.jhou.ast.type.BaseType;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.ast.util.Visitor;
import pl.wojciechkarpiel.jhou.substitution.Substitution;
import pl.wojciechkarpiel.jhou.unifier.AllowedTypeInference;
import pl.wojciechkarpiel.jhou.unifier.DisagreementPair;
import pl.wojciechkarpiel.jhou.unifier.DisagreementSet;
import pl.wojciechkarpiel.jhou.unifier.SolutionIterator;
import pl.wojciechkarpiel.jhou.unifier.tree.Tree;
import pl.wojciechkarpiel.jhou.unifier.tree.WorkWorkNode;
import pl.wojciechkarpiel.jhou.util.DevNullPrintStream;
import pl.wojciechkarpiel.jhou.util.ListUtil;
import pl.wojciechkarpiel.jhou.util.Pair;

import java.io.PrintStream;
import java.util.List;
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

    public static Pair<Term, Term> inferMissing(Pair<Term, Term> termsOfEqualType, AllowedTypeInference allowedTypeInference, PrintStream printStream) {
        List<Term> inputList = ListUtil.of(termsOfEqualType.getLeft(), termsOfEqualType.getRight());
        List<Term> outputList = inferMissingInternal(inputList, allowedTypeInference, printStream);
        return Pair.of(outputList.get(0), outputList.get(1));
    }

    public static List<Term> inferMissing(List<Term> termsOfEqualType, AllowedTypeInference allowedTypeInference, PrintStream printStream) {
        return inferMissingInternal(termsOfEqualType, allowedTypeInference, printStream);
    }

    private static List<Term> inferMissingInternal(List<Term> termsOfEqualType, AllowedTypeInference allowedTypeInference, PrintStream printStream) {
        if (termsOfEqualType.stream().noneMatch(TypeInference::needsInference)) {
            printStream.println("No need for type inference, types fully instantiated");
            return termsOfEqualType;
        }
        if (allowedTypeInference == AllowedTypeInference.NO_INFERENCE_ALLOWED) {
            throw new InferenceRequiredButNotAllowedException();
        }


        Annotations annotations = TypeAnnotator.annotate(termsOfEqualType);

        DisagreementPairCollector disagreementPairCollector = new DisagreementPairCollector(annotations);
        List<DisagreementPair> ds = disagreementPairCollector.collect().getDisagreements();

        for (int i = 0; i < termsOfEqualType.size() - 1; i++) {
            Term prev = termsOfEqualType.get(i);
            Term next = termsOfEqualType.get(i + 1);
            Term anon = annotations.getAnnotation(prev);
            Term anon1 = annotations.getAnnotation(next);
            ds.add(
                    new DisagreementPair(
                            anon,
                            anon1
                    )
            );
        }

        DisagreementSet disagreementSet = new DisagreementSet(ds);

        Tree tree = WorkWorkNode.firstOrderTree(disagreementSet);
        SolutionIterator s = new SolutionIterator(tree, DevNullPrintStream.INSTANCE);
        if (!s.hasNext()){
            throw new CantUnifyTypesException();
        }
        Substitution nxt = s.next();
        TypeRebuilder rebuilder =
                new TypeRebuilder(
                        nxt,
                        annotations,
                        disagreementPairCollector.constantTypeMap(),
                        printStream,
                        allowedTypeInference
                );
        return termsOfEqualType.stream().map(rebuilder::rebuild).collect(Collectors.toList());
    }


    static boolean needsInference(Term term) {
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

    private TypeInference() {
    }

    public static class CantUnifyTypesException extends RuntimeException {
    }

    public static class InferenceRequiredButNotAllowedException extends RuntimeException {
    }

    public static class InferenceHasArbitrarySolutionsException extends RuntimeException {
    }

    final static Type DUMMY_TYPE = BaseType.freshBaseType("dummy_type");
    final static Constant ARROW = Constant.freshConstant(new ArrowType(DUMMY_TYPE, new ArrowType(DUMMY_TYPE, DUMMY_TYPE)), "ARR");
}
