package pl.wojciechkarpiel.unifier.tree;

import pl.wojciechkarpiel.substitution.Substitution;

import java.util.Optional;

public interface Tree {

    // Null at tree root
    Tree getParent();

    boolean itsOver(UsedUpNodes usedUpNodes);

    Optional<WeBackNode> weBack(UsedUpNodes usedUpNodes);

    void expandOnce();

    Substitution inheritedSubstitution();
}
