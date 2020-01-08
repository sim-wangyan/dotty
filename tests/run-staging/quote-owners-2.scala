
import quoted._
import scala.quoted.staging._

object Test {
  given Toolbox = Toolbox.make(getClass.getClassLoader)
  def main(args: Array[String]): Unit = run {
    val q = f(g(Type.IntTag))
    println(q.show)
    '{ println($q) }
  }

  def f(t: Type[List[Int]])(given QuoteContext): Expr[Int] = '{
    def ff: Int = {
      val a: $t = {
        type T = $t
        val b: T = 3 :: Nil
        b
      }
      a.head
    }
    ff
  }

  def g[T](a: Type[T])(given QuoteContext): Type[List[T]] = '[List[$a]]
}
