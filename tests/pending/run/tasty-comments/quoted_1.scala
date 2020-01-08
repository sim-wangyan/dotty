import scala.quoted._
import scala.quoted.autolift.given


object Macros {

  inline def printComment[T](t: => T): Unit =
    ${ impl('t) }

  def impl[T](x: Expr[T])(given qctx: QuoteContext): Expr[Unit] = {
    import qctx.tasty.{_, given}

    val tree = x.unseal
    tree.symbol.comment.map(_.raw) match {
      case Some(str) => '{ println(${str}) }
      case None => '{ println() }
    }
  }
}
