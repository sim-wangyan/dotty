package scala.quoted

package object matching {

  /** Find an implicit of type `T` in the current scope given by `qctx`.
   *  Return `Some` containing the expression of the implicit or
   * `None` if implicit resolution failed.
   *
   *  @tparam T type of the implicit parameter
   *  @param tpe quoted type of the implicit parameter
   *  @param qctx current context
   */
  def summonExpr[T](given tpe: Type[T])(given qctx: QuoteContext): Option[Expr[T]] = {
    import qctx.tasty.{_, given}
    searchImplicit(tpe.unseal.tpe) match {
      case iss: ImplicitSearchSuccess => Some(iss.tree.seal.asInstanceOf[Expr[T]])
      case isf: ImplicitSearchFailure => None
    }
  }

}
