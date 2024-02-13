package pl.wojciechkarpiel.unifier.tree;

import pl.wojciechkarpiel.substitution.Substitution;

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
    public boolean itsOver(UsedUpNodes usedUpNodes) {
        return true;
    }

    @Override
    public Optional<WeBackNode> weBack(UsedUpNodes usedUpNodes) {
        return Optional.empty();
    }

    @Override
    public void expandOnce() {

    }

    @Override
    public Substitution inheritedSubstitution() {
        return null;
    }
}
