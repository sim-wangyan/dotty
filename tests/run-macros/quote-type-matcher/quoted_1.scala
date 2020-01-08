import scala.quoted._
import scala.quoted.matching._


object Macros {

  inline def matches[A, B]: Unit = ${ matchesExpr('[A], '[B]) }

  private def matchesExpr[A, B](a: Type[A], b: Type[B])(given qctx: QuoteContext): Expr[Unit] = {
    import qctx.tasty.{Bind => _, _}

    val res = scala.internal.quoted.Type.unapply[Tuple, Tuple](a)(b, true, qctx).map { tup =>
      tup.toArray.toList.map {
        case r: quoted.Type[_] =>
          s"Type(${r.unseal.show})"
        case r: Sym[_] =>
          s"Sym(${r.name})"
      }
    }

    '{
      println("Scrutinee: " + ${Expr(a.unseal.show)})
      println("Pattern: " + ${Expr(b.unseal.show)})
      println("Result: " + ${Expr(res.toString)})
      println()
    }
  }

}
