import scala.quoted._
import scala.quoted.autolift.given

object Foo {
  inline def foo(): Int = ${bar(${x})} // error
  def x(given QuoteContext): Expr[Int] = '{1}
  def bar(i: Int)(given QuoteContext): Expr[Int] = i
}
