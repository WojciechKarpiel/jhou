package pl.wojciechkarpiel.jhou.types.inference;

import org.junit.jupiter.api.Test;
import pl.wojciechkarpiel.jhou.ast.*;
import pl.wojciechkarpiel.jhou.ast.type.ArrowType;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.substitution.Substitution;
import pl.wojciechkarpiel.jhou.unifier.AllowedTypeInference;
import pl.wojciechkarpiel.jhou.unifier.SolutionIterator;
import pl.wojciechkarpiel.jhou.unifier.UnificationSettings;
import pl.wojciechkarpiel.jhou.unifier.Unifier;
import pl.wojciechkarpiel.jhou.util.ListUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static pl.wojciechkarpiel.jhou.Api.*;

class TypeInferenceTest {


    @Test
    void failStrictMode() {
        Term C = freshConstant(null, "C");
        Type y = freshType("Y");
        Term D = freshConstant(y, "D");
        Abstraction fn = (Abstraction) abstraction(null, "x", x -> app(C, x));
        Term d = app(fn, D);


        {
            UnificationSettings us = new UnificationSettings();
            us.setAllowedTypeInference(AllowedTypeInference.NO_INFERENCE_ALLOWED);
            assertThrows(TypeInference.InferenceRequiredButNotAllowedException.class, () -> Unifier.unify(d, d, us));
        }
        {
            UnificationSettings us = new UnificationSettings();
            us.setAllowedTypeInference(AllowedTypeInference.NO_ARBITRARY_SOLUTIONS);
            assertThrows(TypeInference.InferenceHasArbitrarySolutionsException.class, () -> Unifier.unify(d, d, us));

        }
        {
            UnificationSettings us = new UnificationSettings();
            us.setAllowedTypeInference(AllowedTypeInference.PERMISSIVE);
            SolutionIterator s = Unifier.unify(d, d, us);
            assertEquals(0, s.next().getSubstitution().size());
        }
    }

    @Test
    void inferMissing() {
        Term C = freshConstant(null, "C");
        Type y = freshType("Y");
        Term D = freshConstant(y, "D");
        Abstraction fn = (Abstraction) abstraction(null, "x", x -> app(C, x));
        Term d = app(fn, D);
        Term t = TypeInference.inferMissing(d);

        Constant cRemake = (Constant) ((Application) ((Abstraction) ((Application) t).getFunction()).getBody()).getFunction();

        assertEquals(y, ((ArrowType) cRemake.getType()).getFrom());
        assertNotEquals(y, ((ArrowType) cRemake.getType()).getTo());
//        Term newV = (Con ((Application) t).getBody());
//        assertEquals (y, newV.getType());
    }

    @Test
    void apiUsageExample() {
        // GIVEN
        Term c = freshConstantInferType("C");
        Variable y = freshVariableInferType("y");
        Term left_ = abstractionInferType(x -> app(y, app(c, app(y, x))));
        Term right_ = abstractionInferType(x -> app(c, x));

        List<Term> tt = TypeInference.inferMissing(ListUtil.of(left_, right_));

        Term left = tt.get(0);
        Term right = tt.get(1);
        // WHEN
        // result is an iterator over possible substitutions that unify the two sider
        SolutionIterator result = unify(left, right);

        // THEN
        assertTrue(result.hasNext());
        Substitution solution = result.next();
        assertFalse(result.hasNext()); // only one solution in this case

//         check if shape of substitution is the one we expect
        Type t = ((Abstraction) right).getVariable().getType();
        Term expectedSub = abstraction(t, x -> x);
        assertEquals(1, solution.getSubstitution().size());
        assertEquals(expectedSub, solution.getSubstitution().get(0).getTerm());
        assertEquals("y", solution.getSubstitution().get(0).getVariable().toString());

        // let's also do the substitutions for the final check
        assertNotEquals(left, right);
        assertEquals(
                betaNormalize(solution.substitute(left)),
                betaNormalize(solution.substitute(right))
        );
    }


    @Test
    void apiUsageExamplec() {
        // GIVEN
        Term c = freshConstant(null, "C");
        Variable y = freshVariable(null, "y");
        Term left_ = abstraction(null, x -> app(y, app(c, app(y, x))));
        Term right_ = abstraction(null, x -> app(c, x));

        List<Term> tt = TypeInference.inferMissing(ListUtil.of(left_, right_));

        Term left = tt.get(0);
        Term right = tt.get(1);
        // WHEN
        // result is an iterator over possible substitutions that unify the two sider
        SolutionIterator result = unify(left, right);

        // THEN
        assertTrue(result.hasNext());
        Substitution solution = result.next();
        assertFalse(result.hasNext()); // only one solution in this case

//         check if shape of substitution is the one we expect
        Type t = ((Abstraction) right).getVariable().getType();
        Term expectedSub = abstraction(t, x -> x);
        assertEquals(1, solution.getSubstitution().size());
        assertEquals(expectedSub, solution.getSubstitution().get(0).getTerm());
        assertEquals("y", solution.getSubstitution().get(0).getVariable().toString());

        // let's also do the substitutions for the final check
        assertNotEquals(left, right);
        assertEquals(
                betaNormalize(solution.substitute(left)),
                betaNormalize(solution.substitute(right))
        );
    }


    @Test
    void apiUsageExampleSLOWIFNOTFIRSTORDERHACK() {
        // GIVEN
        Type type = freshType(); // we work with typed lambda calculus, so we need some type
        Term c = freshConstant(arrow(type, type), "C");
        Variable y = freshVariable(arrow(type, type), "y");
        Term left = abstractionInferType("xl", x -> app(y, app(c, app(y, x))));
        Term right = abstractionInferType("xr", x -> app(c, x));


        // WHEN
        // result is an iterator over possible substitutions that unify the two sider
        SolutionIterator result = unify(left, right);

        // THEN
        assertTrue(result.hasNext());
        Substitution solution = result.next();
        assertFalse(result.hasNext()); // only one solution in this case

        // check if shape of substitution is the one we expect
        Substitution expectedSolution = new Substitution(y, abstraction(type, x -> x));
        assertEquals(expectedSolution, solution);

    }

}