# Higher-order unification for JVM

JHou stands for "JVM Higher-order unification".
The library provides algorithm for higher-order unification.

The implementation based on
[this paper](https://www21.in.tum.de/teaching/sar/SS20/5.pdf).

## Theoretical notes

* η-conversion is admitted (i.e. function extensionality assumed)

## Implementation notes

The implementation goal is to make the library widely usable:

* JHou targets bytecode v8
* JHou has no external dependencies

## TODO

* finish MVP, ie match disagreement pairs (section 3.4 from paper)
* basic optimisations