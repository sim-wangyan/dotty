object Macros {
  import scala.quoted._
  import scala.quoted.autolift.given

  inline def go[T](t: => T) = ${ impl('t) }
  def impl[T](expr: Expr[T])(given qctx: QuoteContext): Expr[Unit] = {
    import qctx.tasty.{_, given}

    val tree = expr.unseal

    val methods =
      tree.tpe.classSymbol.get.classMethods.map { m =>
        val name = m.show
        m.tree match
          case ddef: DefDef =>
            val returnType = ddef.returnTpt.tpe.show
            s"$name : $returnType"
      }.sorted

    methods.foldLeft('{}) { (res, m) => '{ $res; println(${m}) } }
  }
}
