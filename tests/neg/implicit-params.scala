object Test {

  case class C(x: Int)
  case class D(x: Int)

  def f(x: Int)(given c: C) = x + c.x

  def g0(x: Int)(given c: C) (y: Int) = x + c.x + y // now OK

  def g(x: Int)(given c: C)(given D) = x + c.x + summon[D].x  // OK

  def h(x: Int) given () = x // error: missing return type

  given C : C(11)
  given D : D(11)

  f(1)
  f(1)(given C)
  f(given 2)  // error
  f(1)(C)   // error

  g(1)    // OK
  g(1)(given C)   // OK
  g(1)(given C)(given D(0)) // OK
  g(1)(given D) // error
  g(1)(D) // error
  g(1)(C)(D) // error
}