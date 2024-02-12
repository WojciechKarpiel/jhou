package pl.wojciechkarpiel.rigidityChecker;

import pl.wojciechkarpiel.ast.*;
import pl.wojciechkarpiel.ast.util.Visitor;
import pl.wojciechkarpiel.normalizer.Normalizer;
import pl.wojciechkarpiel.util.MapUtil;
import pl.wojciechkarpiel.util.Unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A term is called rigid if its head is a bound variable or constant,
 * 3.1
 * be β-reduced; then, it can be written as
 * λx1 . . . xn . @ (t1 , . . . , tp )
 * We call @ the head, {x1 , . . . , xn } the binder, and λx1 . . . xn . @ the heading of
 * the term. The terms t1 , . . . , tp are its arguments, which must be β-normal again.
 * A term is called rigid if its head is a bound variable or constant; otherwise it is
 * called flexible. The intuition is that a rigid heading always stays the same
 */
// SEE SIMPLIFIER
public class RigidityChecker {


    /**
     * Term MUST be a top-level, otherwise it won't take into account free vars!
     */
    public static boolean isRigid(Term term) {
        return isRigid(term, new ArrayList<>());
    }

    public static boolean isRigid(Term term, List<Variable> boundVariablesFromOuterScope) {
        RigidityChecker rigidityChecker = new RigidityChecker();
        boundVariablesFromOuterScope.forEach(b -> rigidityChecker.boundVariables.put(b, Unit.UNIT));
        return rigidityChecker.isRigidInternal(term);
    }

    private RigidityChecker() {
    }


    // TODO I need to keep track of bound variables or assume i'll be always checking true head
    private MapUtil<Variable, Unit> boundVariables = new MapUtil<>(new HashMap<>());


    private boolean isRigidInternal(Term term) {
        Term normalized = Normalizer.normalize(term); // TODO necessary? can cache normalizedness? can WHNF normalizedness?
        return normalized.visit(new Visitor<Boolean>() {
            public Boolean visitConstant(Constant constant) {
                return true;
            }

            @Override
            public Boolean visitVariable(Variable variable) {
                return boundVariables.get(variable).isPresent();
            }

            @Override
            public Boolean visitApplication(Application application) {
                return false;
            }

            @Override
            public Boolean visitAbstraction(Abstraction abstraction) {
                return boundVariables.withMapping(
                        abstraction.getVariable(),
                        Unit.UNIT,
                        () -> isRigidInternal(abstraction.getBody())
                );
            }
        });
    }

}
