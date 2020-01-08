package blah

import scala.language.implicitConversions
import scala.quoted._
import scala.quoted.autolift.given

object AsObject {
  final class LineNo(val lineNo: Int)
  object LineNo {
    def unsafe(i: Int): LineNo = new LineNo(i)
    inline given x : LineNo = ${impl}
    private def impl(given qctx: QuoteContext): Expr[LineNo] = {
      import qctx.tasty.{_, given}
      '{unsafe(${rootPosition.startLine})}
    }
  }
}
