import scala.quoted._
import scala.quoted.matching._


object Macros {

  inline def (self: => StringContext) xyz(args: => String*): String = ${impl('self, 'args)}

  private def impl(self: Expr[StringContext], args: Expr[Seq[String]])(given QuoteContext): Expr[String] = {
    self match {
      case '{ StringContext(${ExprSeq(parts)}: _*) } =>
        val parts2 = Expr.ofList(parts.map(x => '{ $x.reverse }))
        '{ StringContext($parts2: _*).s($args: _*) }
      case _ =>
        '{ "ERROR" }
    }

  }

}
