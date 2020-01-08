import scala.quoted._
import scala.quoted.autolift.given

object Macros {
  inline def foo(inline i: Int, dummy: Int, j: Int): Int = ${ bar(i, 'j) }
  def bar(x: Int, y: Expr[Int])(given QuoteContext): Expr[Int] = '{ ${x} + $y }
}
