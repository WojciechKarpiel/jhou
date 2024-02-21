# Changelog

## 0.9

* Performance improvement: search tree pruning
* Performance: alpha equality check done lazily
  (i.e. alpha equality check does not eagerly replace variables in the entire abstraction body)
* Bugfix: Variables with inferred types can be safely substituted

## 0.8

* Partial type inference for input formulas

## 0.7

* Change publishing site to Maven Central

## 0.6

* Bugfix: Matching procedure provided projections which mismatched the type of the flexible variable

## 0.5

* Bugfix: Substitution was applied in incorrect order, leaving free variables unsubstituted