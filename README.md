# Higher-order unification for JVM

JHou stands for "JVM Higher-order unification".
The library provides algorithm for higher-order unification.

The implementation based on
[this paper](https://www21.in.tum.de/teaching/sar/SS20/5.pdf).

## Usage

**TODO**

Example of unifying `λx.y (C (y x))` and `λx.C x` is in the
[Unit tests](src/test/java/pl/wojciechkarpiel/unifier/UnifierTest.java).
The result is `y → λx.x`, pretty cool huh?

```
Term a = ...;
Term b = ...;
Substitution s = Unifier.unify(a, b);
System.out.println(s)
-> Substitution{[{y -> (fn[x]x)}]}
```

## Theoretical notes

* η-conversion is admitted (i.e. function extensionality assumed),
  see notes in chapter 4 of aforementioned paper

Above claim is a smart-sounding claim from the paper, tbh I don't understand it fully,
because the algorithm doesn't unify `λx.C x` and `C`,
so not automatically doing eta conversion. If you're capable of clarifying, LMK please

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