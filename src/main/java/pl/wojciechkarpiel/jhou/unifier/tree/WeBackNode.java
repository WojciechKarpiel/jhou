package pl.wojciechkarpiel.jhou.unifier.tree;

import pl.wojciechkarpiel.jhou.substitution.Substitution;
import pl.wojciechkarpiel.jhou.substitution.SubstitutionPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WeBackNode implements Tree {

    private final Tree parent;
    private final Substitution substitution;
    private boolean usedUp;

    public WeBackNode(Tree parent, Substitution substitution) {
        this.parent = parent;
        this.substitution = substitution;
        this.usedUp = false;
    }

    public Tree getParent() {
        return parent;
    }

    @Override
    public boolean itsOver() {
        return usedUp;
    }

    @Override
    public Optional<WeBackNode> weBack() {
        if (itsOver()) return Optional.empty();
        else return Optional.of(this);
    }

    @Override
    public void expandOnce() {
    }

    @Override
    public Substitution inheritedSubstitution() {
        return substitution;
    }

    public Substitution fullSolution() {
        List<Substitution> res = new ArrayList<>();
        for (Tree p = this; p != null; p = p.getParent()) {
            Substitution e = p.inheritedSubstitution();
            if (e != null) res.add(e);
        }
        List<SubstitutionPair> r = new ArrayList<>();
        for (int i = res.size() - 1; i >= 0; i--) {
            Substitution s = res.get(i);
            r.addAll(s.getSubstitution());
        }
        return new Substitution(r);
    }

    public void markUsedUp() {
        this.usedUp = true;
    }
}
