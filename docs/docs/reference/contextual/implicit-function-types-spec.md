---
layout: doc-page
title: "Implicit Function Types - More Details"
---

## Syntax

    Type              ::=  ...
                        |  FunArgTypes ‘=>’ Typee
    FunArgTypes       ::=  InfixType
                        |  ‘(’ [ ‘[given]’ FunArgType {‘,’ FunArgType } ] ‘)’
                        |  ‘(’ ‘[given]’ TypedFunParam {‘,’ TypedFunParam } ‘)’
    Bindings          ::=  ‘(’ [[‘given’] Binding {‘,’ Binding}] ‘)’

Implicit function types associate to the right, e.g.
`(given S) => (given T) => U` is the same as `(given S) => ((given T) => U)`.

## Implementation

Implicit function types are shorthands for class types that define `apply`
methods with implicit parameters. Specifically, the `N`-ary function type
`T1, ..., TN => R` is a shorthand for the class type
`ImplicitFunctionN[T1 , ... , TN, R]`. Such class types are assumed to have the following definitions, for any value of `N >= 1`:
```scala
package scala
trait ImplicitFunctionN[-T1 , ... , -TN, +R] {
  def apply(given x1: T1 , ... , xN: TN): R
}
```
Implicit function types erase to normal function types, so these classes are
generated on the fly for typechecking, but not realized in actual code.

Implicit function literals `(given x1: T1, ..., xn: Tn) => e` map
implicit parameters `xi` of types `Ti` to the result of evaluating the expression `e`.
The scope of each implicit parameter `xi` is `e`. The parameters must have pairwise distinct names.

If the expected type of the implicit function literal is of the form
`scala.ImplicitFunctionN[S1, ..., Sn, R]`, the expected type of `e` is `R` and
the type `Ti` of any of the parameters `xi` can be omitted, in which case `Ti
= Si` is assumed. If the expected type of the implicit function literal is
some other type, all implicit parameter types must be explicitly given, and the expected type of `e` is undefined.
The type of the implicit function literal is `scala.ImplicitFunctionN[S1, ...,Sn, T]`, where `T` is the widened
type of `e`. `T` must be equivalent to a type which does not refer to any of
the implicit parameters `xi`.

The implicit function literal is evaluated as the instance creation
expression
```scala
new scala.ImplicitFunctionN[T1, ..., Tn, T] {
  def apply(given x1: T1, ..., xn: Tn): T = e
}
```
An implicit parameter may also be a wildcard represented by an underscore `_`. In
that case, a fresh name for the parameter is chosen arbitrarily.

Note: The closing paragraph of the
[Anonymous Functions section](https://www.scala-lang.org/files/archive/spec/2.12/06-expressions.html#anonymous-functions)
of Scala 2.12 is subsumed by implicit function types and should be removed.

Implicit function literals `(given x1: T1, ..., xn: Tn) => e` are
automatically created for any expression `e` whose expected type is
`scala.ImplicitFunctionN[T1, ..., Tn, R]`, unless `e` is
itself a implicit function literal. This is analogous to the automatic
insertion of `scala.Function0` around expressions in by-name argument position.

Implicit function types generalize to `N > 22` in the same way that function types do, see [the corresponding
documentation](../dropped-features/limit22.md).

## Examples

See the section on Expressiveness from [Simplicitly: foundations and
applications of implicit function
types](https://dl.acm.org/citation.cfm?id=3158130).

### Type Checking

After desugaring no additional typing rules are required for implicit function types.
