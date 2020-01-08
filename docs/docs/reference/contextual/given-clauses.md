---
layout: doc-page
title: "Given Parameters"
---

Functional programming tends to express most dependencies as simple function parameterization.
This is clean and powerful, but it sometimes leads to functions that take many parameters and
call trees where the same value is passed over and over again in long call chains to many
functions. Given clauses can help here since they enable the compiler to synthesize
repetitive arguments instead of the programmer having to write them explicitly.

For example, with the [given instances](./delegates.md) defined previously,
a maximum function that works for any arguments for which an ordering exists can be defined as follows:
```scala
def max[T](x: T, y: T)(given ord: Ord[T]): T =
  if (ord.compare(x, y) < 0) y else x
```
Here, `ord` is an _implicit parameter_ introduced with a `given` clause.
The `max` method can be applied as follows:
```scala
max(2, 3)(given intOrd)
```
The `(given intOrd)` part passes `intOrd` as an argument for the `ord` parameter. But the point of
implicit parameters is that this argument can also be left out (and it usually is). So the following
applications are equally valid:
```scala
max(2, 3)
max(List(1, 2, 3), Nil)
```

## Anonymous Given Clauses

In many situations, the name of an implicit parameter need not be
mentioned explicitly at all, since it is used only in synthesized arguments for
other implicit parameters. In that case one can avoid defining a parameter name
and just provide its type. Example:
```scala
def maximum[T](xs: List[T])(given Ord[T]): T =
  xs.reduceLeft(max)
```
`maximum` takes an implicit parameter of type `Ord` only to pass it on as an
inferred argument to `max`. The name of the parameter is left out.

Generally, implicit parameters may be defined either as a full parameter list `(given p_1: T_1, ..., p_n: T_n)` or just as a sequence of types `(given T_1, ..., T_n)`.
Vararg given parameters are not supported.

## Inferring Complex Arguments

Here are two other methods that have an implicit parameter of type `Ord[T]`:
```scala
def descending[T](given asc: Ord[T]): Ord[T] = new Ord[T] {
  def compare(x: T, y: T) = asc.compare(y, x)
}

def minimum[T](xs: List[T])(given Ord[T]) =
  maximum(xs)(given descending)
```
The `minimum` method's right hand side passes `descending` as an explicit argument to `maximum(xs)`.
With this setup, the following calls are all well-formed, and they all normalize to the last one:
```scala
minimum(xs)
maximum(xs)(given descending)
maximum(xs)(given descending(given listOrd))
maximum(xs)(given descending(given listOrd(given intOrd)))
```

## Multiple Given Clauses

There can be several `given` parameter clauses in a definition and `given` parameter clauses can be freely
mixed with normal ones. Example:
```scala
def f(u: Universe)(given ctx: u.Context)(given s: ctx.Symbol, k: ctx.Kind) = ...
```
Multiple given clauses are matched left-to-right in applications. Example:
```scala
object global extends Universe { type Context = ... }
given ctx  : global.Context { type Symbol = ...; type Kind = ... }
given sym  : ctx.Symbol
given kind : ctx.Kind
```
Then the following calls are all valid (and normalize to the last one)
```scala
f
f(global)
f(global)(given ctx)
f(global)(given ctx)(given sym, kind)
```
But `f(global)(given sym, kind)` would give a type error.

## Summoning Instances

The method `summon` in `Predef` returns the given instance of a specific type. For example,
the given instance for `Ord[List[Int]]` is produced by
```scala
summon[Ord[List[Int]]]  // reduces to listOrd given intOrd
```
The `summon` method is simply defined as the (non-widening) identity function over an implicit parameter.
```scala
def summon[T](given x: T): x.type = x
```

## Syntax

Here is the new syntax of parameters and arguments seen as a delta from the [standard context free syntax of Scala 3](../../internals/syntax.md).
```
ClsParamClauses     ::=  ...
                      |  {ClsParamClause} {GivenClsParamClause}
GivenClsParamClause ::=  ‘(’ ‘given’ (ClsParams | GivenTypes) ‘)’
DefParamClauses     ::=  ...
                      |  {DefParamClause} {GivenParamClause}
GivenParamClause    ::=  ‘(’ ‘given’ (DefParams | GivenTypes) ‘)’
GivenTypes          ::=  AnnotType {‘,’ AnnotType}

ParArgumentExprs    ::=  ...
                      |  ‘(’ ‘given’ ExprsInParens ‘)’
```
