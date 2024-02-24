package pl.wojciechkarpiel.jhou.unifier.tree;

import pl.wojciechkarpiel.jhou.substitution.Substitution;

import java.util.Optional;

public class NagmiNode implements Tree {
    private final Tree parent;

    public NagmiNode(Tree parent) {
        this.parent = parent;
    }

    public Tree getParent() {
        return parent;
    }

    @Override
    public boolean itsOver() {
        return true;
    }

    @Override
    public Optional<WeBackNode> weBack() {
        return Optional.empty();
    }

    @Override
    public void expandOnce() {
    }

    @Override
    public Substitution inheritedSubstitution() {
        throw new UnsupportedOperationException();
    }
}
