import scala.quoted._

class Foo {

  def f(given QuoteContext): Unit = '{
    def bar[T](x: T): T = x
    bar[
      this.type  // error
      ] {
      this  // error
    }
  }

  inline def i(): Unit = ${ Foo.impl[Any]('{
    given QuoteContext = ???
    'this // error
  }) }

  inline def j(that: Foo): Unit = ${ Foo.impl[Any]('{
     given QuoteContext = ???
    'that // error
  }) }

}

object Foo {
  def impl[T](x: Any)(given QuoteContext): Expr[Unit] = '{}
}
