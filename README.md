# Higher-order unification for JVM

JHou stands for "JVM Higher-order unification".
The library provides algorithm for higher-order unification.

The implementation based on
[this paper](https://www21.in.tum.de/teaching/sar/SS20/5.pdf).

## Installation

Add following dependency to your project

```xml
<dependency>
  <groupId>pl.wojciechkarpiel</groupId>
  <artifactId>jhou</artifactId>
  <version>0.9</version>
</dependency>
```

For other build tools (SBT, Gradle, Leiningen) see the configuration snippets
[here](https://central.sonatype.com/artifact/pl.wojciechkarpiel/jhou)

## Usage

See the
[API definition](src/main/java/pl/wojciechkarpiel/jhou/Api.java).

See this [unit test](src/test/java/pl/wojciechkarpiel/jhou/api/ApiTest.java)
for an API usage example with walk-through comments.
This is a quickstart disguised as a unit test.

```java
import pl.wojciechkarpiel.jhou.ast.Term;
import pl.wojciechkarpiel.jhou.ast.Variable;
import pl.wojciechkarpiel.jhou.ast.type.Type;
import pl.wojciechkarpiel.jhou.substitution.Substitution;
import pl.wojciechkarpiel.jhou.unifier.SolutionIterator;

import static pl.wojciechkarpiel.jhou.Api.*;

public class Main {

  /**
   * Example of unifying `λx.y (C (y x))` and `λx.C x`.
   * The result is `y → λx.x`
   */
  public static void main(String[] args) {
    Type type = freshType(); // we work with typed lambda calculus, so we need some type
    Term c = freshConstant(arrow(type, type), "C");
    Variable y = freshVariable(arrow(type, type), "y");
    Term left = abstraction(x -> app(y, app(c, app(y, x))));
    Term right = abstraction(x -> app(c, x));
    // result is an iterator over possible substitutions that unify the two sides
    SolutionIterator result = unify(left, right);
    Substitution solution = result.next();
    System.out.println(solution);
    // prints: Substitution{[{y → λV_7.V_7}]}
  }
}
```

### API overview

The `pl.wojciechkarpiel.jhou.Api` class contains static methods
described below.

#### Type construction

The library deals with higher-order unification in a simply *typed* lambda calculus,
therefore one needs to create types for the term. If your unification problem
doesn't care about types, then create a single base type and arrow types based on it.

* `Type freshType()` - create a new base type
* `Type freshType(String name)` - create a new base type and assign it a name.
  The name is only for pretty-printing, two fresh types of the same name are distinct.
* `Type arrow(Type from, Type to)` - create an arrow type (i.e. a function type).
* `Type typeOfCurriedFunction(Type arg1, Type arg2, Type... argN)` - helper for nested
  arrow types, equivalent to `arrow(arg1, arrow(arg2, arrow(arg3, ...)))`

Example:

```java
Type nat = freshType("ℕ");
Type natToNat = arrow(nat, nat);
```

### Term construction

`Term.equals` is raw structural equality. For other notions of equality, see "Normalization and utility" section.

* `Variable freshVariable(Type type)` - create a new free variable of type `type`. A `Variable` is a subtype of `Term`
  Substitution for such variables will be searched for by the unification procedure
* `Variable freshVariable()` - like above, but with type automatically inferred during unification
* `Variable freshVariable(Type type, String name)` - Name is only for pretty-printing, two fresh variables of the same
  name are distinct.
* `Variable freshVariable(String name)` - like above, but with type automatically inferred during unification
* `Term freshConstant(Type type)`
* `Term freshConstant()` - like above, but with type automatically inferred during unification
* `Term freshConstant(Type type, String name)` - Name is only for pretty-printing, two fresh constants of the same name
  are distinct.
* `Term freshConstant(String name)` - like above, but with type automatically inferred during unification
* `Term application(Term function, Term argument)`
* `Term app(Term function, Term argument)` - same as `application`
* `Term abstraction(Type variableType, Function<Variable, Term> abstraction)`
* `Term abstraction(Function<Variable, Term> abstraction)`  - like above, but with type automatically inferred during
  unification
* `Term abstraction(Type variableType, String variableName, Function<Variable, Term> abstraction)` - Name is only for
  pretty-printing
* `Term abstraction(String variableName, Function<Variable, Term> abstraction)` - like above, but with type
  automatically inferred during unification

### Normalization and utility

* `Term betaNormalize(Term term)`
* `Term etaContract(Term term)` - Simplifies every occurrence of λx.fx into f.
  Note that this is different from the eta-transformation
  in the beta-eta normal form `betaEtaNormalize`
* `Term etaExpand(Term term)`
* `Term betaEtaNormalForm(Term term)` - term in beta-eta normal form (see paper)
* `Term betaNormalEtaContracted(Term term)` - equivalent to `etaContract(betaNormalize(term)`
* `Type typeOf(Term term)`
* `boolean alphaEqual(Term a, Term b)`
* `boolean alphaBetaEtaEqual(Term a, Term b)`

### Unification

Note: unification for higher-order logics is undecidable, hence the algorithm with
no time bound might run forever without producing any result.

* `SolutionIterator unify(Term a, Term b)` - try to unify terms `a` and `b` with default UnificationSettings.
* `SolutionIterator unify(Term a, Term b, UnificationSettings unificationSettings)` - see UnificationSettings
  description

#### UnificationSettings

`UnificationSettings` is an aggregation of 3 settings:

* `int maxIterations` (default: unlimited) - Add a computation bound on the unification.
  The bound is loosely defined as the max height of the search tree.
  For example,
  [unifying `λx.y (C (y x))` and `λx.C x`](src/test/java/pl/wojciechkarpiel/jhou/api/ApiTest.java)
  needs a bound of 2 to find a solution, and bound of 3 to exhaust the search space.
* `PrintStream printStream` (default: `System.out`) - use a different `PrintStream` instead of `System.out` -
  this can be used to suppress logging.
* `AllowedTypeInference allowedTypeInference` (default: `PERMISSIVE`) - controls what happens in case type inference is
  needed:
  * `NO_INFERENCE_ALLOWED` - throw an exception in case types aren't fully specified in advance
  * `NO_ARBITRARY_SOLUTIONS` - allow type inference only if it generates a unique solution, otherwise throw
    an exception. For example, the type of `λx.x` is `t → t` for an *arbitrary* `t`, hence it would not be allowed
  * `PERMISSIVE` - allow type inference, and in case the solution is non-unique, arbitrarily fill in the missing types.
    For example, inferred type for `λx.x` is `t → t`, for an arbitrary type `t`.

#### SolutionIterator

`SolutionIterator` returned by the `unify()` methods is an iterator over `Substitution`.
It's methods are:

* `boolean hasNext()` - inherited from `Iterator`
* `Substitution next()` - inherited from `Iterator`
* `Optional<Substitution> peek()` - returns next element from the iterator, but does not consume it.
  Returns `Optional.empty()` iff `hasNext()` is false.
* `boolean searchLimitReached()` - when `hasNext()` returns `false`, one can check
  if no more solutions are found because of search limit reached (see `UnificationSettings`),
  or if there are truly no more solutions.

#### Substitution

`Substitution` is a solution for the unification problem. It's methods are:

* `Term substitute(Term)` - return a term with free variables substituted.
  If used on a free variable, then returns the substitution for that variable.
  Leaves unknown free variables unchanged.

#### Example

```
Variable variable = freshVariable(type)
SolutionIterator solutionIterator = unify(leftSide(variable), rightSide(variable));
if (solutionIterator.hasNext()) {
  Substitution s = solutionIterator.next();
  Term result = s.substitute(variable);
}
```

## Complete example

TODO: let there be a number expression and find a solution for f x = a * a.
Transform between ASTs

<!-- See [Number expression example](src/test/java/pl/wojciechkarpiel/jhou/example/Example1.java) -->

## Theoretical notes

* η-conversion is admitted (i.e. function extensionality assumed),
  see notes in chapter 4 of aforementioned paper

## Implementation notes

The implementation goal is to make the library widely usable:

* JHou targets bytecode v8
* JHou has no external dependencies

## TODO

* describe how to use
* provide usage examples
* refactor
* stop disappointing my parents and find a gf