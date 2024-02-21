package pl.wojciechkarpiel.jhou.unifier.tree;

import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.ast.Variable;
import pl.wojciechkarpiel.jhou.normalizer.Normalizer;
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

public class WorkWorkNode implements Tree {

    private final Substitution fromParent;
    private final DisagreementSet disagreementSet;
    private final boolean pretendYoureDoingFirstOrder;

    private List<Tree> children;
    private final Tree parent;

    public WorkWorkNode(Tree parent, Substitution fromParent, DisagreementSet disagreementSet) {
        this(parent, fromParent, disagreementSet, false);
    }

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
            boolean itsOver = children.stream().allMatch(Tree::itsOver);
            // prune the tree
            if (itsOver) {
                children = ListUtil.of(new NagmiNode(this));
            }
            return itsOver;
        }
    }

    @Override
    public Optional<WeBackNode> weBack() {
        if (children == null) return Optional.empty();
        return children.stream()
                .map(Tree::weBack)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    @Override
    public void expandOnce() {
        if (children != null) {
            children.forEach(Tree::expandOnce);
        } else {
            SimplificationResult simplify = Simplifier.simplify(disagreementSet);

            WorkWorkNode thiz = this;
            children = simplify.visit(new SimplificationVisitor<List<Tree>>() {
                @Override
                public List<Tree> visitSuccess(SimplificationSuccess success) {
                    return ListUtil.of(new WeBackNode(thiz, success.getSolution()));
                }

                @Override
                public List<Tree> visitNode(SimplificationNode node) {
                    return createChildNodes(node.getDisagreement());
                }

                @Override
                public List<Tree> visitFailure(NonUnifiable nonUnifiable) {
                    return ListUtil.of(new NagmiNode(thiz));
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
        List<List<Substitution>> stream = disagreement.getDisagreements().stream()
                .filter(dp -> dp.getType() == PairType.RIGID_FLEXIBLE)
                .map(disagreementPair ->
                        Matcher.matchS(disagreementPair.getMostRigid(), disagreementPair.getLeastRigid())
                ).collect(Collectors.toList());
        List<Substitution> qflatSubs = new ArrayList<>();
        stream.forEach(qflatSubs::addAll);
        List<Substitution> flatSubs = deduplicateSort(qflatSubs);
        if (pretendYoureDoingFirstOrder) { // hack to seed up first order search
            // this works because in 1st order search any found substitution must be a good one,
            // so we don't need to create multiple tree branches (effectively turning exponential into linear)
            if (!flatSubs.isEmpty()) flatSubs = ListUtil.of(flatSubs.get(0));
        }
        for (Substitution possibleSolution : flatSubs) {
            DisagreementSet newDs = new DisagreementSet(disagreement.getDisagreements().stream().map(disagreementPair ->
                    new DisagreementPair(
                            Normalizer.betaNormalize(possibleSolution.substitute(disagreementPair.getMostRigid().backToTerm())),
                            Normalizer.betaNormalize(possibleSolution.substitute(disagreementPair.getLeastRigid().backToTerm()))
                    )).collect(Collectors.toList()));

            Tree tree = new WorkWorkNode(this, possibleSolution, newDs, pretendYoureDoingFirstOrder);
            result.add(tree);
        }

        return result;
    }

    /*
     * optiomize flatsubs in a folllowing way:
     *
     * 1. deduplicate subs, ie let all x,S(x) be different
     * 2. Sort by count of sobstitutions for a var
     * 3. generate children only for the least used var (but keep the other disagreement pairs!)
     */

    // assume all subs are singles
    private static List<Substitution> deduplicateSort(List<Substitution> input) {
        if (input.isEmpty()) return input;

        Map<Variable, Set<Term>> dedupByVar = new HashMap<>(input.size());
        for (Substitution pair_ : input) {
            if (pair_.getSubstitution().size() > 1) throw new RuntimeException();
            if (pair_.getSubstitution().isEmpty()) continue;
            SubstitutionPair pair = pair_.getSubstitution().get(0);
            Set<Term> s_ = new HashSet<>();
            Set<Term> s = dedupByVar.putIfAbsent(pair.getVariable(), s_);
            if (s == null) s = s_;
            s.add(pair.getTerm());
        }
        if (dedupByVar.isEmpty()) {
            return input;
        }

        List<Pair<Variable, Set<Term>>> least = new ArrayList<>();
        int minCount = dedupByVar.values().stream().mapToInt(Set::size).min().getAsInt();
        dedupByVar.entrySet().stream().filter(p -> p.getValue().size() == minCount)
                .forEach(p -> least.add(Pair.of(p.getKey(), p.getValue())));

        least.sort(Comparator.comparingInt(o -> o.getLeft().getId().getId()));

        return least.get(0).getRight().stream().map(sbs ->
                new Substitution(least.get(0).getLeft(), sbs)).collect(Collectors.toList());

    }
}












