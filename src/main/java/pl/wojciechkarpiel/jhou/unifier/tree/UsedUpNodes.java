package pl.wojciechkarpiel.jhou.unifier.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Mark nodes you no longer have interest in
 */
public class UsedUpNodes {

    private final List<WeBackNode> usedUp = new ArrayList<>();

    private UsedUpNodes() {
    }

    public static UsedUpNodes empty() {
        return new UsedUpNodes();
    }

    public void addUsedUpNode(WeBackNode node) {
        usedUp.add(node);
    }

    public boolean isUsedUp(WeBackNode node) {
        return usedUp.contains(node);
    }
}
