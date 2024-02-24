package pl.wojciechkarpiel.jhou.unifier.tree;

import pl.wojciechkarpiel.jhou.ast.Equality;
import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.ast.Variable;
import pl.wojciechkarpiel.jhou.substitution.Substitution;
import pl.wojciechkarpiel.jhou.substitution.SubstitutionPair;
import pl.wojciechkarpiel.jhou.unifier.DisagreementPair;
import pl.wojciechkarpiel.jhou.unifier.DisagreementSet;
import pl.wojciechkarpiel.jhou.unifier.PairType;
import pl.wojciechkarpiel.jhou.unifier.simplifier.Matcher;
import pl.wojciechkarpiel.jhou.unifier.simplifier.Simplifier;
import pl.wojciechkarpiel.jhou.unifier.simplifier.result.*;
import pl.wojciechkarpiel.jhou.util.ListUtil;
import pl.wojciechkarpiel.jhou.util.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WorkWorkNode implements Tree {

    private final Substitution fromParent;
    private final DisagreementSet disagreementSet;
    private final boolean pretendYoureDoingFirstOrder;

    private List<Tree> children;
    private final Tree parent;

    public WorkWorkNode(Tree parent, Substitution fromParent, DisagreementSet disagreementSet) {
        this(parent, fromParent, disagreementSet, false);
    }

    /**
     * @param pretendYoureDoingFirstOrder total hack, don't use. TODO: write a separate 1st order unification class
     */
    public WorkWorkNode(Tree parent, Substitution fromParent, DisagreementSet disagreementSet, boolean pretendYoureDoingFirstOrder) {
        this.parent = parent;
        this.fromParent = fromParent;
        this.disagreementSet = disagreementSet;
        this.pretendYoureDoingFirstOrder = pretendYoureDoingFirstOrder;
    }

    public Tree getParent() {
        return parent;
    }

    @Override
    public boolean itsOver() {
        if (children == null) return false;
        else {
            pruneDirectChildren();
            boolean itsOver = children.stream().allMatch(Tree::itsOver);
            // prune the tree
            if (itsOver && !fatherOfSingleFailure()) {
                children = ListUtil.of(new NagmiNode(this));
            }
            return itsOver;
        }
    }

    @Override
    public Optional<WeBackNode> weBack() {
        if (children == null) return Optional.empty();
        pruneDirectChildren();
        return children.stream()
                .map(Tree::weBack)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    @Override
    public void expandOnce() {
        if (children != null) {
            pruneDirectChildren();
            children.forEach(Tree::expandOnce);
        } else {
            SimplificationResult simplify = Simplifier.simplify(disagreementSet);
            children = simplify.visit(new SimplificationVisitor<List<Tree>>() {
                @Override
                public List<Tree> visitSuccess(SimplificationSuccess success) {
                    return ListUtil.of(new WeBackNode(WorkWorkNode.this, success.getSolution()));
                }

                @Override
                public List<Tree> visitNode(SimplificationNode node) {
                    return createChildNodes(node.getDisagreement());
                }

                @Override
                public List<Tree> visitFailure(NonUnifiable nonUnifiable) {
                    return ListUtil.of(new NagmiNode(WorkWorkNode.this));
                }
            });
        }
    }

    @Override
    public Substitution inheritedSubstitution() {
        return fromParent;
    }

    private List<Tree> createChildNodes(DisagreementSet disagreement) {
        List<Tree> result = new ArrayList<>();
        Stream<Substitution> flattenedSubstitutionStream = disagreement.getDisagreements().stream()
                .filter(dp -> dp.getType() == PairType.RIGID_FLEXIBLE)
                .flatMap(disagreementPair ->
                        Matcher.match(disagreementPair.getMostRigid(), disagreementPair.getLeastRigid())
                                .intoSubstitutionStream());
        List<Substitution> flattenedSubstitutions;
        if (pretendYoureDoingFirstOrder) { // hack to seed up first order search
            // this works because in 1st order search any found substitution must be a good one,
            // so we don't need to create multiple tree branches (effectively turning exponential into linear)
            // but really, a much saner option would be to wire another class for 1st order unification, it's simple
            flattenedSubstitutions = flattenedSubstitutionStream.findFirst().map(ListUtil::of).orElse(ListUtil.of());
        } else {
            flattenedSubstitutions = deduplicateSort(flattenedSubstitutionStream.collect(Collectors.toList()));
        }
        for (Substitution possibleSolution : flattenedSubstitutions) {
            DisagreementSet newDisagreementSet = new DisagreementSet(disagreement.getDisagreements().stream()
                    .map(disagreementPair ->
                            new DisagreementPair(
                                    possibleSolution.substitute(disagreementPair.getMostRigid().backToTerm()),
                                    possibleSolution.substitute(disagreementPair.getLeastRigid().backToTerm())
                            )).collect(Collectors.toList()));

            Tree tree = new WorkWorkNode(this, possibleSolution, newDisagreementSet, pretendYoureDoingFirstOrder);
            result.add(tree);
        }
        return result;
    }

    /*
     * Optimization pass aimed at minimizing tree branching
     *
     * 1. Deduplicate substitutions, identifying alpha-equal ones
     * 2. Sort by count of possible substitution for a given variable
     * 3. generate children only for the least used var (but keep the other disagreement pairs!)
     *
     * method assumes that all inputs are single-variable substitutions
     */
    private static List<Substitution> deduplicateSort(List<Substitution> input) {
        if (input.isEmpty()) return input;
        // List<Term> and not Set<Term> because of alpha-equality problem
        Map<Variable, List<Term>> dedupByVar = new HashMap<>(input.size());
        for (Substitution substitution : input) {
            if (substitution.getSubstitution().size() > 1) throw new RuntimeException();
            if (substitution.getSubstitution().isEmpty()) continue;
            SubstitutionPair pair = substitution.getSubstitution().get(0);
            dedupByVar.putIfAbsent(pair.getVariable(), new ArrayList<>());
            List<Term> termsForVar = dedupByVar.get(pair.getVariable());
            // could be Set.add if not for alpha equality (bruijn would fix the problem of hashcode-equals for alpha)
            if (termsForVar.stream().noneMatch(t -> Equality.alphaEqual(t, pair.getTerm()))) {
                termsForVar.add(pair.getTerm());
            }
        }
        if (dedupByVar.isEmpty()) {
            return input;
        }

        List<Pair<Variable, List<Term>>> least = new ArrayList<>();
        int minCount = dedupByVar.values().stream().mapToInt(List::size).min().getAsInt();
        dedupByVar.entrySet().stream().filter(p -> p.getValue().size() == minCount)
                .forEach(p -> least.add(Pair.of(p.getKey(), p.getValue())));
        least.sort(Comparator.comparingInt(o -> o.getLeft().getId().getId()));
        Pair<Variable, List<Term>> minSized = least.get(0);
        return minSized.getRight().stream()
                // Sorting here doesn't help the algorithm, it's here to ensure deterministic outputs
                // (technically it's not truly deterministic because of hashcode overlap, but it's good enough)
                .sorted(Comparator.comparingInt(Term::hashCode))
                .map(term -> new Substitution(minSized.getLeft(), term))
                .collect(Collectors.toList());
    }

    private void pruneDirectChildren() {
        if (fatherOfSingleFailure()) return;
        if (children.stream().anyMatch(WorkWorkNode::canBePruned)) {
            children = children.stream().filter(c -> !WorkWorkNode.canBePruned(c)).collect(Collectors.toList());
            if (children.isEmpty()) children = ListUtil.of(new NagmiNode(this));
        }
    }

    /**
     * Checks only direct children
     */
    private static boolean canBePruned(Tree child) {
        if (child instanceof NagmiNode) return child.itsOver();
        if (child instanceof WeBackNode) return child.itsOver();
        return false;
    }

    private boolean fatherOfSingleFailure() {
        return children.size() == 1 && children.get(0) instanceof NagmiNode;
    }
}
