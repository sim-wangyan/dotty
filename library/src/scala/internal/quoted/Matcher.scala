package scala.internal.quoted

import scala.annotation.internal.sharable

import scala.quoted._
import scala.quoted.matching.Sym

private[quoted] object Matcher {

  class QuoteMatcher[QCtx <: QuoteContext & Singleton](given val qctx: QCtx) {
    // TODO improve performance

    // TODO use flag from qctx.tasty.rootContext. Maybe -debug or add -debug-macros
    private final val debug = false

    import qctx.tasty.{_, given}
    import Matching._

    /** A map relating equivalent symbols from the scrutinee and the pattern
     *  For example in
     *  ```
     *  '{val a = 4; a * a} match case '{ val x = 4; x * x }
     *  ```
     *  when matching `a * a` with `x * x` the enviroment will contain `Map(a -> x)`.
     */
    private type Env = Map[Symbol, Symbol]

    inline private def withEnv[T](env: Env)(body: => (given Env) => T): T = body(given env)

    class SymBinding(val sym: Symbol, val fromAbove: Boolean)

    def termMatch(scrutineeTerm: Term, patternTerm: Term, hasTypeSplices: Boolean): Option[Tuple] = {
      implicit val env: Env = Map.empty
      if (hasTypeSplices) {
        implicit val ctx: Context = internal.Context_GADT_setFreshGADTBounds(rootContext)
        val matchings = scrutineeTerm.underlyingArgument =?= patternTerm.underlyingArgument
        // After matching and doing all subtype checks, we have to aproximate all the type bindings
        // that we have found and seal them in a quoted.Type
        matchings.asOptionOfTuple.map { tup =>
          Tuple.fromArray(tup.toArray.map { // TODO improve performace
            case x: SymBinding => internal.Context_GADT_approximation(summon[Context])(x.sym, !x.fromAbove).seal
            case x => x
          })
        }
      }
      else {
        scrutineeTerm.underlyingArgument =?= patternTerm.underlyingArgument
      }
    }

    // TODO factor out common logic with `termMatch`
    def typeTreeMatch(scrutineeTypeTree: TypeTree, patternTypeTree: TypeTree, hasTypeSplices: Boolean): Option[Tuple] = {
      implicit val env: Env = Map.empty
      if (hasTypeSplices) {
        implicit val ctx: Context = internal.Context_GADT_setFreshGADTBounds(rootContext)
        val matchings = scrutineeTypeTree =?= patternTypeTree
        // After matching and doing all subtype checks, we have to aproximate all the type bindings
        // that we have found and seal them in a quoted.Type
        matchings.asOptionOfTuple.map { tup =>
          Tuple.fromArray(tup.toArray.map { // TODO improve performace
            case x: SymBinding => internal.Context_GADT_approximation(summon[Context])(x.sym, !x.fromAbove).seal
            case x => x
          })
        }
      }
      else {
        scrutineeTypeTree =?= patternTypeTree
      }
    }

    private def hasBindTypeAnnotation(tpt: TypeTree): Boolean = tpt match {
      case Annotated(tpt2, annot) => isBindAnnotation(annot) || hasBindTypeAnnotation(tpt2)
      case _ => false
    }

    private def hasBindAnnotation(sym: Symbol) = sym.annots.exists(isBindAnnotation)

    private def hasFromAboveAnnotation(sym: Symbol) = sym.annots.exists(isFromAboveAnnotation)

    private def isBindAnnotation(tree: Tree): Boolean = tree match {
      case New(tpt) => tpt.symbol == internal.Definitions_InternalQuoted_patternBindHoleAnnot
      case annot => annot.symbol.owner == internal.Definitions_InternalQuoted_patternBindHoleAnnot
    }

    private def isFromAboveAnnotation(tree: Tree): Boolean = tree match {
      case New(tpt) => tpt.symbol == internal.Definitions_InternalQuoted_fromAboveAnnot
      case annot => annot.symbol.owner == internal.Definitions_InternalQuoted_fromAboveAnnot
    }

    /** Check that all trees match with `mtch` and concatenate the results with && */
    private def matchLists[T](l1: List[T], l2: List[T])(mtch: (T, T) => Matching): Matching = (l1, l2) match {
      case (x :: xs, y :: ys) => mtch(x, y) && matchLists(xs, ys)(mtch)
      case (Nil, Nil) => matched
      case _ => notMatched
    }

    private given treeListOps: extension (scrutinees: List[Tree]) with

      /** Check that all trees match with =?= and concatenate the results with && */
      def =?= (patterns: List[Tree])(given Context, Env): Matching =
        matchLists(scrutinees, patterns)(_ =?= _)

    end treeListOps

    private given treeOps: extension (scrutinee0: Tree) with

      /** Check that the trees match and return the contents from the pattern holes.
       *  Return None if the trees do not match otherwise return Some of a tuple containing all the contents in the holes.
       *
       *  @param scrutinee The tree beeing matched
       *  @param pattern The pattern tree that the scrutinee should match. Contains `patternHole` holes.
       *  @param `summon[Env]` Set of tuples containing pairs of symbols (s, p) where s defines a symbol in `scrutinee` which corresponds to symbol p in `pattern`.
       *  @return `None` if it did not match or `Some(tup: Tuple)` if it matched where `tup` contains the contents of the holes.
       */
      def =?= (pattern0: Tree)(given Context, Env): Matching = {

        /** Normalize the tree */
        def normalize(tree: Tree): Tree = tree match {
          case Block(Nil, expr) => normalize(expr)
          case Block(stats1, Block(stats2, expr)) => normalize(Block(stats1 ::: stats2, expr))
          case Inlined(_, Nil, expr) => normalize(expr)
          case _ => tree
        }

        val scrutinee = normalize(scrutinee0)
        val pattern = normalize(pattern0)

        /** Check that both are `val` or both are `lazy val` or both are `var` **/
        def checkValFlags(): Boolean = {
          import Flags._
          val sFlags = scrutinee.symbol.flags
          val pFlags = pattern.symbol.flags
          sFlags.is(Lazy) == pFlags.is(Lazy) && sFlags.is(Mutable) == pFlags.is(Mutable)
        }

        def bindingMatch(sym: Symbol) =
          matched(new Sym(sym.name, sym))

        (scrutinee, pattern) match {

          // Match a scala.internal.Quoted.patternHole typed as a repeated argument and return the scrutinee tree
          case (scrutinee @ Typed(s, tpt1), Typed(TypeApply(patternHole, tpt :: Nil), tpt2))
              if patternHole.symbol == internal.Definitions_InternalQuoted_patternHole &&
                 s.tpe <:< tpt.tpe &&
                 tpt2.tpe.derivesFrom(defn.RepeatedParamClass) =>
            matched(scrutinee.seal)

          // Match a scala.internal.Quoted.patternHole and return the scrutinee tree
          case (ClosedPatternTerm(scrutinee), TypeApply(patternHole, tpt :: Nil))
              if patternHole.symbol == internal.Definitions_InternalQuoted_patternHole &&
                 scrutinee.tpe <:< tpt.tpe =>
            matched(scrutinee.seal)

          // Matches an open term and wraps it into a lambda that provides the free variables
          case (scrutinee, pattern @ Apply(Select(TypeApply(patternHole, List(Inferred())), "apply"), args0 @ IdentArgs(args)))
              if patternHole.symbol == internal.Definitions_InternalQuoted_patternHole =>
            def bodyFn(lambdaArgs: List[Tree]): Tree = {
              val argsMap = args.map(_.symbol).zip(lambdaArgs.asInstanceOf[List[Term]]).toMap
              new TreeMap {
                override def transformTerm(tree: Term)(given ctx: Context): Term =
                  tree match
                    case tree: Ident => summon[Env].get(tree.symbol).flatMap(argsMap.get).getOrElse(tree)
                    case tree => super.transformTerm(tree)
              }.transformTree(scrutinee)
            }
            val names = args.map(_.name)
            val argTypes = args0.map(x => x.tpe.widenTermRefExpr)
            val resType = pattern.tpe
            val res = Lambda(MethodType(names)(_ => argTypes, _ => resType), bodyFn)
            matched(res.seal)

          //
          // Match two equivalent trees
          //

          case (Literal(constant1), Literal(constant2)) if constant1 == constant2 =>
            matched

          case (Typed(expr1, tpt1), Typed(expr2, tpt2)) =>
            expr1 =?= expr2 && tpt1 =?= tpt2

          case (scrutinee, Typed(expr2, _)) =>
            scrutinee =?= expr2

          case (Ident(_), Ident(_)) if scrutinee.symbol == pattern.symbol || summon[Env].get(scrutinee.symbol).contains(pattern.symbol) =>
            matched

          case (Select(qual1, _), Select(qual2, _)) if scrutinee.symbol == pattern.symbol =>
            qual1 =?= qual2

          case (_: Ref, _: Ref) if scrutinee.symbol == pattern.symbol =>
            matched

          case (Apply(fn1, args1), Apply(fn2, args2)) if fn1.symbol == fn2.symbol || summon[Env].get(fn1.symbol).contains(fn2.symbol) =>
            fn1 =?= fn2 && args1 =?= args2

          case (TypeApply(fn1, args1), TypeApply(fn2, args2)) if fn1.symbol == fn2.symbol || summon[Env].get(fn1.symbol).contains(fn2.symbol) =>
            fn1 =?= fn2 && args1 =?= args2

          case (Block(stats1, expr1), Block(binding :: stats2, expr2)) if isTypeBinding(binding) =>
            qctx.tasty.internal.Context_GADT_addToConstraint(summon[Context])(binding.symbol :: Nil)
            matched(new SymBinding(binding.symbol, hasFromAboveAnnotation(binding.symbol))) && Block(stats1, expr1) =?= Block(stats2, expr2)

          case (Block(stat1 :: stats1, expr1), Block(stat2 :: stats2, expr2)) =>
            val newEnv = (stat1, stat2) match {
              case (stat1: Definition, stat2: Definition) =>
                summon[Env] + (stat1.symbol -> stat2.symbol)
              case _ =>
                summon[Env]
            }
            withEnv(newEnv) {
              stat1 =?= stat2 && Block(stats1, expr1) =?= Block(stats2, expr2)
            }

          case (scrutinee, Block(typeBindings, expr2)) if typeBindings.forall(isTypeBinding) =>
            val bindingSymbols = typeBindings.map(_.symbol)
            qctx.tasty.internal.Context_GADT_addToConstraint(summon[Context])(bindingSymbols)
            bindingSymbols.foldRight(scrutinee =?= expr2)((x, acc) => matched(new SymBinding(x, hasFromAboveAnnotation(x))) && acc)

          case (If(cond1, thenp1, elsep1), If(cond2, thenp2, elsep2)) =>
            cond1 =?= cond2 && thenp1 =?= thenp2 && elsep1 =?= elsep2

          case (Assign(lhs1, rhs1), Assign(lhs2, rhs2)) =>
            val lhsMatch =
              if ((lhs1 =?= lhs2).isMatch) matched
              else notMatched
            lhsMatch && rhs1 =?= rhs2

          case (While(cond1, body1), While(cond2, body2)) =>
            cond1 =?= cond2 && body1 =?= body2

          case (New(tpt1), New(tpt2)) =>
            tpt1 =?= tpt2

          case (This(_), This(_)) if scrutinee.symbol == pattern.symbol =>
            matched

          case (Super(qual1, mix1), Super(qual2, mix2)) if mix1 == mix2 =>
            qual1 =?= qual2

          case (Repeated(elems1, _), Repeated(elems2, _)) if elems1.size == elems2.size =>
            elems1 =?= elems2

          case (scrutinee: TypeTree, pattern: TypeTree) if scrutinee.tpe <:< pattern.tpe =>
            matched

          case (Applied(tycon1, args1), Applied(tycon2, args2)) =>
            tycon1 =?= tycon2 && args1 =?= args2

          case (ValDef(_, tpt1, rhs1), ValDef(_, tpt2, rhs2)) if checkValFlags() =>
            val bindMatch =
              if (hasBindAnnotation(pattern.symbol) || hasBindTypeAnnotation(tpt2)) bindingMatch(scrutinee.symbol)
              else matched
            def rhsEnv = summon[Env] + (scrutinee.symbol -> pattern.symbol)
            bindMatch && tpt1 =?= tpt2 && treeOptMatches(rhs1, rhs2)(given summon[Context], rhsEnv)

          case (DefDef(_, typeParams1, paramss1, tpt1, Some(rhs1)), DefDef(_, typeParams2, paramss2, tpt2, Some(rhs2))) =>
            val bindMatch =
              if (hasBindAnnotation(pattern.symbol)) bindingMatch(scrutinee.symbol)
              else matched
            def rhsEnv =
              summon[Env] + (scrutinee.symbol -> pattern.symbol) ++
                typeParams1.zip(typeParams2).map((tparam1, tparam2) => tparam1.symbol -> tparam2.symbol) ++
                paramss1.flatten.zip(paramss2.flatten).map((param1, param2) => param1.symbol -> param2.symbol)

            bindMatch &&
              typeParams1 =?= typeParams2 &&
              matchLists(paramss1, paramss2)(_ =?= _) &&
              tpt1 =?= tpt2 &&
              withEnv(rhsEnv)(rhs1 =?= rhs2)

          case (Closure(_, tpt1), Closure(_, tpt2)) =>
            // TODO match tpt1 with tpt2?
            matched

          case (Match(scru1, cases1), Match(scru2, cases2)) =>
            scru1 =?= scru2 && matchLists(cases1, cases2)(caseMatches)

          case (Try(body1, cases1, finalizer1), Try(body2, cases2, finalizer2)) =>
            body1 =?= body2 && matchLists(cases1, cases2)(caseMatches) && treeOptMatches(finalizer1, finalizer2)

          // Ignore type annotations
          case (Annotated(tpt, _), _) =>
            tpt =?= pattern
          case (_, Annotated(tpt, _)) =>
            scrutinee =?= tpt

          // No Match
          case _ =>
            if (debug)
              println(
                s""">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                   |Scrutinee
                   |  ${scrutinee.show}
                   |
                   |${scrutinee.showExtractors}
                   |
                   |did not match pattern
                   |  ${pattern.show}
                   |
                   |${pattern.showExtractors}
                   |
                   |with environment: ${summon[Env]}
                   |
                   |
                   |""".stripMargin)
            notMatched
        }
      }
    end treeOps

    private object ClosedPatternTerm {
      /** Matches a term that does not contain free variables defined in the pattern (i.e. not defined in `Env`) */
      def unapply(term: Term)(given Context, Env): Option[term.type] =
        if freePatternVars(term).isEmpty then Some(term) else None

      /** Return all free variables of the term defined in the pattern (i.e. defined in `Env`) */
      def freePatternVars(term: Term)(given qctx: Context, env: Env): Set[Symbol] =
        val accumulator = new TreeAccumulator[Set[Symbol]] {
          def foldTree(x: Set[Symbol], tree: Tree)(given ctx: Context): Set[Symbol] =
            tree match
              case tree: Ident if env.contains(tree.symbol) => foldOverTree(x + tree.symbol, tree)
              case _ => foldOverTree(x, tree)
        }
        accumulator.foldTree(Set.empty, term)
    }

    private object IdentArgs {
      def unapply(args: List[Term])(given Context): Option[List[Ident]] =
        args.foldRight(Option(List.empty[Ident])) {
          case (id: Ident, Some(acc)) => Some(id :: acc)
          case (Block(List(DefDef("$anonfun", Nil, List(params), Inferred(), Some(Apply(id: Ident, args)))), Closure(Ident("$anonfun"), None)), Some(acc))
              if params.zip(args).forall(_.symbol == _.symbol) =>
            Some(id :: acc)
          case _ => None
        }
    }

    private def treeOptMatches(scrutinee: Option[Tree], pattern: Option[Tree])(given Context, Env): Matching = {
      (scrutinee, pattern) match {
        case (Some(x), Some(y)) => x =?= y
        case (None, None) => matched
        case _ => notMatched
      }
    }

    private def caseMatches(scrutinee: CaseDef, pattern: CaseDef)(given Context, Env): Matching = {
      val (caseEnv, patternMatch) = patternsMatches(scrutinee.pattern, pattern.pattern)
      withEnv(caseEnv) {
        patternMatch &&
          treeOptMatches(scrutinee.guard, pattern.guard) &&
          scrutinee.rhs =?= pattern.rhs
      }
    }


    /** Check that the pattern trees match and return the contents from the pattern holes.
      *  Return a tuple with the new environment containing the bindings defined in this pattern and a matching.
      *  The matching is None if the pattern trees do not match otherwise return Some of a tuple containing all the contents in the holes.
      *
      *  @param scrutinee The pattern tree beeing matched
      *  @param pattern The pattern tree that the scrutinee should match. Contains `patternHole` holes.
      *  @param `summon[Env]` Set of tuples containing pairs of symbols (s, p) where s defines a symbol in `scrutinee` which corresponds to symbol p in `pattern`.
      *  @return The new environment containing the bindings defined in this pattern tuppled with
      *          `None` if it did not match or `Some(tup: Tuple)` if it matched where `tup` contains the contents of the holes.
      */
    private def patternsMatches(scrutinee: Tree, pattern: Tree)(given Context, Env): (Env, Matching) = (scrutinee, pattern) match {
      case (v1: Term, Unapply(TypeApply(Select(patternHole @ Ident("patternHole"), "unapply"), List(tpt)), Nil, Nil))
          if patternHole.symbol.owner == summon[Context].requiredModule("scala.runtime.quoted.Matcher") =>
        (summon[Env], matched(v1.seal))

      case (Ident("_"), Ident("_")) =>
        (summon[Env], matched)

      case (Bind(name1, body1), Bind(name2, body2)) =>
        val bindEnv = summon[Env] + (scrutinee.symbol -> pattern.symbol)
        patternsMatches(body1, body2)(given summon[Context], bindEnv)

      case (Unapply(fun1, implicits1, patterns1), Unapply(fun2, implicits2, patterns2)) =>
        val (patEnv, patternsMatch) = foldPatterns(patterns1, patterns2)
        (patEnv, patternsMatches(fun1, fun2)._2 && implicits1 =?= implicits2 && patternsMatch)

      case (Alternatives(patterns1), Alternatives(patterns2)) =>
        foldPatterns(patterns1, patterns2)

      case (Typed(Ident("_"), tpt1), Typed(Ident("_"), tpt2)) =>
        (summon[Env], tpt1 =?= tpt2)

      case (v1: Term, v2: Term) =>
        (summon[Env], v1 =?= v2)

      case _ =>
        if (debug)
          println(
            s""">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                |Scrutinee
                |  ${scrutinee.show}
                |
                |${scrutinee.showExtractors}
                |
                |did not match pattern
                |  ${pattern.show}
                |
                |${pattern.showExtractors}
                |
                |with environment: ${summon[Env]}
                |
                |
                |""".stripMargin)
        (summon[Env], notMatched)
    }

    private def foldPatterns(patterns1: List[Tree], patterns2: List[Tree])(given Context, Env): (Env, Matching) = {
      if (patterns1.size != patterns2.size) (summon[Env], notMatched)
      else patterns1.zip(patterns2).foldLeft((summon[Env], matched)) { (acc, x) =>
        val (env, res) = patternsMatches(x._1, x._2)(given summon[Context], acc._1)
        (env, acc._2 && res)
      }
    }

    private def isTypeBinding(tree: Tree): Boolean = tree match {
      case tree: TypeDef => hasBindAnnotation(tree.symbol)
      case _ => false
    }
  }

  /** Result of matching a part of an expression */
  private opaque type Matching = Option[Tuple]

  private object Matching {

    def notMatched: Matching = None
    val matched: Matching = Some(())
    def matched(x: Any): Matching = Some(Tuple1(x))

    def (self: Matching) asOptionOfTuple: Option[Tuple] = self

    /** Concatenates the contents of two sucessful matchings or return a `notMatched` */
    def (self: Matching) && (that: => Matching): Matching = self match {
      case Some(x) =>
        that match {
          case Some(y) => Some(x ++ y)
          case _ => None
        }
      case _ => None
    }

    /** Is this matching the result of a successful match */
    def (self: Matching) isMatch: Boolean = self.isDefined

    /** Joins the mattchings into a single matching. If any matching is `None` the result is `None`.
     *  Otherwise the result is `Some` of the concatenation of the tupples.
     */
    def foldMatchings(matchings: Matching*): Matching = {
      // TODO improve performance
      matchings.foldLeft[Matching](Some(())) {
        case (Some(acc), Some(holes)) => Some(acc ++ holes)
        case (_, _) => None
      }
    }

  }

}
