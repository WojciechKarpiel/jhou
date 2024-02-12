# Higher-order unification for JVM

JHou stands for "JVM Higher-order unification".
The library provides algorithm for higher-order unification.

The implementation based on
[this paper](https://www21.in.tum.de/teaching/sar/SS20/5.pdf).
Deviations from the paper:

* Use DeBruijn indices instead of named variables, because
  I couldn't figure out when can I safely alpha-convert
  in beta-eta-normal form

## Implementation notes

The implementation goal is to make the library widely usable:

* JHou targets bytecode v8
* JHou has no external dependencies

## TODO

* simplify disagreement set
* type checking (well-typednes ensures safety of beta-reduction)
* Optimize normalization (HNF wherever possible)
* rigity checker test