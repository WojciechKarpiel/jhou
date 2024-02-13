package pl.wojciechkarpiel.unifier.tree;

import pl.wojciechkarpiel.substitution.Substitution;
import pl.wojciechkarpiel.substitution.SubstitutionPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WeBackNode implements Tree {

    private final Tree parent;
    private final Substitution substitution;

    public WeBackNode(Tree parent, Substitution substitution) {
        this.parent = parent;
        this.substitution = substitution;
    }

    public Substitution getSubstitution() {
        return substitution;
    }

    public Tree getParent() {
        return parent;
    }

    @Override
    public boolean itsOver() {
        return false;
    }

    @Override
    public Optional<WeBackNode> weBack() {
        return Optional.of(this);
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
}
