# Higher-order unification for JVM

JHou stands for "JVM Higher-order unification".
The library provides algorithm for higher-order unification.

The implementation based on
[this paper](https://www21.in.tum.de/teaching/sar/SS20/5.pdf).

## Installation

Add following dependency to your project

```xml
<project>
  <repositories>
    <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
    </repository>
  </repositories>
  <dependencies>
    <dependency>
      <groupId>com.github.WojciechKarpiel</groupId>
      <artifactId>jhou</artifactId>
      <version>v0.4</version>
    </dependency>
  </dependencies>
</project>
```

If you're using other build tool (Gradle, SBT, Leiningen),
[here](https://jitpack.io/#WojciechKarpiel/jhou)
are the configuration snippets. `v0.4` is the newest release.

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
    Term left = abstraction(type, x -> app(y, app(c, app(y, x))));
    Term right = abstraction(type, x -> app(c, x));
    // result is an iterator over possible substitutions that unify the two sides
    SolutionIterator result = unify(left, right);
    Substitution solution = result.next();
    System.out.println(solution);
    // prints: Substitution{[{y → λV_7.V_7}]}
  }
}
```

## Theoretical notes

* η-conversion is admitted (i.e. function extensionality assumed),
  see notes in chapter 4 of aforementioned paper

## Implementation notes

The implementation goal is to make the library widely usable:

* JHou targets bytecode v8
* JHou has no external dependencies

## TODO

* imlpement user-facing API
* describe how to use
* refactor
* publish
* spend the rest of the PTO doing something else than coding
* stop disappointing my parents and find a gf