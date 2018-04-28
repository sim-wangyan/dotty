package tasty

object definitions {

// ====== Names ======================================

  trait Name
  trait PossiblySignedName

  enum TermName extends Name with PossiblySignedName {
    case Simple(str: String)
    case Qualified(prefix: TermName, selector: String)              // s"$prefix.$name"

    case DefaultGetter(methodName: TermName, idx: String)           // s"$methodName${"$default$"}${idx+1}"
    case Variant(underlying: TermName, covariant: Boolean)          // s"${if (covariant) "+" else "-"}$underlying"
    case SuperAccessor(underlying: TermName)                        // s"${"super$"}$underlying"
    case ProtectedAccessor(underlying: TermName)                    // s"${"protected$"}$underlying"
    case ProtectedSetter(underlying: TermName)                      // s"${"protected$set"}$underlying"
    case ObjectClass(underlying: TermName)                          // s"$underlying${"$"}"
  }

  case class SignedName(name: TermName, resultSig: TypeName, paramSigs: List[TypeName]) extends PossiblySignedName

  case class TypeName(name: TermName) extends Name

// ====== Positions ==================================

  case class Position(firstOffset: Int, lastOffset: Int, sourceFile: String) {
    def startLine: Int = ???
    def startColumn: Int = ???
    def endLine: Int = ???
    def endColumn: Int = ???
  }

  trait Positioned {
    def pos: Position = ???
  }

// ====== Trees ======================================

  trait Tree extends Positioned

// ------ Statements ---------------------------------

  sealed trait TopLevelStatement extends Tree
  sealed trait Statement extends TopLevelStatement

  case class PackageClause(pkg: Term, body: List[TopLevelStatement]) extends TopLevelStatement

  case class Import(expr: Term, selector: List[ImportSelector]) extends Statement

  enum ImportSelector {
    case SimpleSelector(id: Id)
    case RenameSelector(id1: Id, id2: Id)
    case OmitSelector(id1: Id)
  }

  case class Id(name: String) extends Positioned     // untyped ident

// ------ Definitions ---------------------------------

  trait Definition {
    def owner: Definition = ???
  }

  // Does DefDef need a `def tpe: MethodType | PolyType`?
  case class ValDef(name: TermName, tpt: TypeTree, rhs: Option[Term]) extends Definition {
    def mods: List[Modifier] = ???
  }
  case class DefDef(name: TermName, typeParams: List[TypeDef], paramss: List[List[ValDef]],
                    returnTpt: TypeTree, rhs: Option[Term]) extends Definition {
    def mods: List[Modifier] = ???
  }
  case class TypeDef(name: TypeName, rhs: TypeTree | TypeBoundsTree) extends Definition {
    def mods: List[Modifier] = ???
  }
  case class ClassDef(name: TypeName, constructor: DefDef, parents: List[Term | TypeTree],
                      self: Option[ValDef], body: List[Statement]) extends Definition {
    def mods: List[Modifier] = ???
  }
  case class PackageDef(name: TermName, members: List[Statement]) extends Definition

// ------ Terms ---------------------------------

  /** Trees denoting terms */
  enum Term extends Statement {
    def tpe: Type = ???
    case Ident(name: TermName, override val tpe: Type)
    case Select(prefix: Term, name: PossiblySignedName)
    case Literal(value: Constant)
    case This(id: Option[Id])
    case New(tpt: TypeTree)
    case Throw(expr: Term)
    case NamedArg(name: TermName, arg: Term)
    case Apply(fn: Term, args: List[Term])
    case TypeApply(fn: Term, args: List[TypeTree])
    case Super(thiz: Term, mixin: Option[Id])
    case Typed(expr: Term, tpt: TypeTree)
    case Assign(lhs: Term, rhs: Term)
    case Block(stats: List[Statement], expr: Term)
    case Inlined(call: Term, bindings: List[Definition], expr: Term)
    case Lambda(method: Term, tpt: Option[TypeTree])
    case If(cond: Term, thenPart: Term, elsePart: Term)
    case Match(scrutinee: Term, cases: List[CaseDef])
    case Try(body: Term, catches: List[CaseDef], finalizer: Option[Term])
    case Return(expr: Term)
    case Repeated(args: List[Term])
    case SelectOuter(from: Term, levels: Int, target: Type) // can be generated by inlining
  }

  /** Trees denoting types */
  enum TypeTree extends Tree {
    def tpe: Type = ???
    case Synthetic()
    case Ident(name: TypeName, override val tpe: Type)
    case Select(prefix: Term, name: TypeName)
    case Singleton(ref: Term)
    case Refined(underlying: TypeTree, refinements: List[Definition])
    case Applied(tycon: TypeTree, args: List[TypeTree])
    case Annotated(tpt: TypeTree, annotation: Term)
    case And(left: TypeTree, right: TypeTree)
    case Or(left: TypeTree, right: TypeTree)
    case ByName(tpt: TypeTree)
  }

  /** Trees denoting type bounds*/
  case class TypeBoundsTree(loBound: TypeTree, hiBound: TypeTree) extends Tree {
    def tpe: Type.TypeBounds = ???
  }

  /** Trees denoting patterns */
  enum Pattern extends Tree {
    def tpe: Type = ???
    case Value(v: Term)
    case Bind(name: TermName, pat: Pattern)
    case Unapply(unapply: Term, implicits: List[Term], pats: List[Pattern])
    case Alternative(pats: List[Pattern])
    case TypeTest(tpt: TypeTree)
  }

  /** Tree denoting pattern match case */
  case class CaseDef(pat: Pattern, guard: Option[Term], rhs: Term) extends Tree

// ====== Types ======================================

  sealed trait Type

  object Type {
    private val PlaceHolder = ConstantType(Constant.Unit)

    case class ConstantType(value: Constant) extends Type
    case class SymRef(sym: Definition, qualifier: Type | NoPrefix = NoPrefix) extends Type
    case class NameRef(name: Name, qualifier: Type | NoPrefix = NoPrefix) extends Type // NoPrefix means: select from _root_
    case class SuperType(thistp: Type, underlying: Type) extends Type
    case class Refinement(underlying: Type, name: Name, tpe: Type | TypeBounds) extends Type
    case class AppliedType(tycon: Type, args: List[Type | TypeBounds]) extends Type
    case class AnnotatedType(underlying: Type, annotation: Term) extends Type
    case class AndType(left: Type, right: Type) extends Type
    case class OrType(left: Type, right: Type) extends Type
    case class ByNameType(underlying: Type) extends Type
    case class ParamRef(binder: LambdaType[_, _, _], idx: Int) extends Type
    case class ThisType(tp: Type) extends Type
    case class RecursiveThis(binder: RecursiveType) extends Type

    case class RecursiveType private (private var _underlying: Type) extends Type {
      def underlying = _underlying
    }
    object RecursiveType {
      def apply(underlyingExp: RecursiveType => Type) = {
        val rt = new RecursiveType(PlaceHolder) {}
        rt._underlying = underlyingExp(rt)
        rt
      }
    }

    abstract class LambdaType[ParamName, ParamInfo, This <: LambdaType[ParamName, ParamInfo, This]](
      val companion: LambdaTypeCompanion[ParamName, ParamInfo, This]
    ) extends Type {
      private[Type] var _pinfos: List[ParamInfo]
      private[Type] var _restpe: Type

      def paramNames: List[ParamName]
      def paramInfos: List[ParamInfo] = _pinfos
      def resultType: Type = _restpe
    }

    abstract class LambdaTypeCompanion[ParamName, ParamInfo, This <: LambdaType[ParamName, ParamInfo, This]] {
      def apply(pnames: List[ParamName], ptypes: List[ParamInfo], restpe: Type): This

      def apply(pnames: List[ParamName], ptypesExp: This => List[ParamInfo], restpeExp: This => Type): This = {
        val lambda = apply(pnames, Nil, PlaceHolder)
        lambda._pinfos = ptypesExp(lambda)
        lambda._restpe = restpeExp(lambda)
        lambda
      }
    }

    case class MethodType(paramNames: List[TermName], private[Type] var _pinfos: List[Type], private[Type] var _restpe: Type)
    extends LambdaType[TermName, Type, MethodType](MethodType) {
      def isImplicit = (companion `eq` ImplicitMethodType) || (companion `eq` ErasedImplicitMethodType)
      def isErased = (companion `eq` ErasedMethodType) || (companion `eq` ErasedImplicitMethodType)
    }

    case class PolyType(paramNames: List[TypeName], private[Type] var _pinfos: List[TypeBounds], private[Type] var _restpe: Type)
    extends LambdaType[TypeName, TypeBounds, PolyType](PolyType)

    case class TypeLambda(paramNames: List[TypeName], private[Type] var _pinfos: List[TypeBounds], private[Type] var _restpe: Type)
    extends LambdaType[TypeName, TypeBounds, TypeLambda](TypeLambda)

    object TypeLambda extends LambdaTypeCompanion[TypeName, TypeBounds, TypeLambda]
    object PolyType   extends LambdaTypeCompanion[TypeName, TypeBounds, PolyType]
    object MethodType extends LambdaTypeCompanion[TermName, Type, MethodType]

    class SpecializedMethodTypeCompanion extends LambdaTypeCompanion[TermName, Type, MethodType] { self =>
      def apply(pnames: List[TermName], ptypes: List[Type], restpe: Type): MethodType =
        new MethodType(pnames, ptypes, restpe) { override val companion = self }
    }
    object ImplicitMethodType       extends SpecializedMethodTypeCompanion
    object ErasedMethodType         extends SpecializedMethodTypeCompanion
    object ErasedImplicitMethodType extends SpecializedMethodTypeCompanion

    case class TypeBounds(loBound: Type, hiBound: Type)

    case class NoPrefix()
    object NoPrefix extends NoPrefix
  }

// ====== Modifiers ==================================


  enum Modifier {
    case Flags(flags: FlagSet)
    case QualifiedPrivate(boundary: Type)
    case QualifiedProtected(boundary: Type)
    case Annotation(tree: Term)
  }

  trait FlagSet {
    def isProtected: Boolean
    def isAbstract: Boolean
    def isFinal: Boolean
    def isSealed: Boolean
    def isCase: Boolean
    def isImplicit: Boolean
    def isErased: Boolean
    def isLazy: Boolean
    def isOverride: Boolean
    def isInline: Boolean
    def isMacro: Boolean                 // inline method containing toplevel splices
    def isStatic: Boolean                // mapped to static Java member
    def isObject: Boolean                // an object or its class (used for a ValDef or a ClassDef extends Modifier respectively)
    def isTrait: Boolean                 // a trait (used for a ClassDef)
    def isLocal: Boolean                 // used in conjunction with Private/private[Type] to mean private[this] extends Modifier proctected[this]
    def isSynthetic: Boolean             // generated by Scala compiler
    def isArtifact: Boolean              // to be tagged Java Synthetic
    def isMutable: Boolean               // when used on a ValDef: a var
    def isLabel: Boolean                 // method generated as a label
    def isFieldAccessor: Boolean         // a getter or setter
    def isCaseAcessor: Boolean           // getter for class parameter
    def isCovariant: Boolean             // type parameter marked “+”
    def isContravariant: Boolean         // type parameter marked “-”
    def isScala2X: Boolean               // Imported from Scala2.x
    def isDefaultParameterized: Boolean  // Method with default parameters
    def isStable: Boolean                // Method that is assumed to be stable
  }

// ====== Constants ==================================

  enum Constant(val value: Any) {
    case Unit                        extends Constant(())
    case Null                        extends Constant(null)
    case Boolean(v: scala.Boolean)   extends Constant(v)
    case Byte(v: scala.Byte)         extends Constant(v)
    case Short(v: scala.Short)       extends Constant(v)
    case Char(v: scala.Char)         extends Constant(v)
    case Int(v: scala.Int)           extends Constant(v)
    case Long(v: scala.Long)         extends Constant(v)
    case Float(v: scala.Float)       extends Constant(v)
    case Double(v: scala.Double)     extends Constant(v)
    case String(v: java.lang.String) extends Constant(v)
    case Class(v: Type)              extends Constant(v)
    case Enum(v: Type)               extends Constant(v)
  }
}

// --- A sample extractor ------------------

// The abstract class, that's what we export to macro users
abstract class Tasty {

  type Type
  trait AbstractType {
    // exported type fields
  }
  implicit def TypeDeco(x: Type): AbstractType

  type Symbol
  trait AbstractSymbol {
    // exported symbol fields
  }
  implicit def SymbolDeco(s: Symbol): AbstractSymbol

  type Context
  trait AbstractContext {
    val owner: Symbol
    // more exported fields
  }
  implicit def ContextDeco(x: Context): AbstractContext

  type Position
  trait AbstractPosition {
    val start: Int
    val end: Int
    // more fields
  }
  implicit def PositionDeco(p: Position): AbstractPosition

  trait TypedPositioned {
    val pos: Position
    val tpe: Type
  }

  type Pattern
  implicit def PatternDeco(p: Pattern): TypedPositioned

  type Term
  implicit def TermDeco(t: Term): TypedPositioned

  type CaseDef
  implicit def CaseDefDeco(c: CaseDef): TypedPositioned

  val CaseDef: CaseDefExtractor
  abstract class CaseDefExtractor {
    def apply(pat: Pattern, guard: Term, rhs: Term)(implicit ctx: Context): CaseDef
    def unapply(x: CaseDef): Some[(Pattern, Term, Term)]
  }
  // and analogously for all other concrete trees, patterns, types, etc
}

// The concrete implementation - hidden from users.
object TastyImpl extends Tasty {
  import definitions._
  import dotty.tools.dotc._
  import ast.tpd
  import core.{Types, Symbols, Contexts}
  import util.{Positions}

  type Type = Types.Type
  implicit class TypeDeco(x: Type) extends AbstractType {}

  type Symbol = Symbols.Symbol
  implicit class SymbolDeco(s: Symbol) extends AbstractSymbol {}

  type Context = Contexts.Context
  implicit class ContextDeco(c: Context) extends AbstractContext {
    val owner = c.owner
  }

  type Position = Positions.Position
  implicit class PositionDeco(p: Position) extends AbstractPosition {
    val start = p.start
    val end = p.end
  }

  type Pattern = tpd.Tree
  implicit class PatternDeco(p: Pattern) extends TypedPositioned {
    val pos = p.pos
    val tpe = p.tpe
  }

  type Term = tpd.Tree
  implicit class TermDeco(t: Term) extends TypedPositioned {
    val pos = t.pos
    val tpe = t.tpe
  }

  type CaseDef = tpd.CaseDef
  implicit class CaseDefDeco(c: CaseDef) extends TypedPositioned {
    val pos = c.pos
    val tpe = c.tpe
  }

  object CaseDef extends CaseDefExtractor {
    def apply(pat: Pattern, guard: Term, rhs: Term)(implicit ctx: Context): CaseDef =
      tpd.CaseDef(pat, guard, rhs)
    def unapply(x: CaseDef): Some[(Pattern, Term, Term)] =
      Some((x.pat, x.guard, x.body))
  }
}

/* Dependencies:

    the reflect library (which is probably part of stdlib) contains a

      val tasty: Tasty

    this val is implemented reflectively, loading TastyImpl on demand. TastyImpl in turn
    depends on `tools.dotc`.

*/


/* If the dotty implementations all inherit the ...Abstract traits,
   and the Abstract traits inherit thmeselves from ProductN, we can
   also do the following, faster implementation.
   This still does full information hiding, but should be almost
   as fast as native access.

object TastyImpl extends TastyAST {
  import definitions._
  import dotty.tools.dotc._
  import ast.tpd
  import core.{Types, Symbols, Contexts}
  import util.{Positions}

  type Type = Types.Type
  implicit def TypeDeco(x: Type) = x

  type Symbol = Symbols.Symbol
  implicit def SymbolDeco(s: Symbol) = s

  type Context = Contexts.Context
  implicit def ContextDeco(c: Context) = c

  type Position = Positions.Position
  implicit def PositionDeco(p: Position) = p

  type Pattern = tpd.Tree
  implicit def PatternDeco(p: Pattern) = p

  type Term = tpd.Tree
  implicit def TermDeco(t: Term) = t

  type CaseDef = tpd.CaseDef
  implicit def CaseDefDeco(c: CaseDef) = c

  object CaseDef extends CaseDefExtractor {
    def apply(pat: Pattern, guard: Term, rhs: Term)(implicit ctx: Context): CaseDef =
      tpd.CaseDef(pat, guard, rhs)
    def unapply(x: CaseDef): AbstractCaseDef = x
  }
}

This approach is fast because all accesses work without boxing. But there are also downsides:

1. The added reflect supertypes for the dotty types might have a negative performance
   impact for normal compilation.

2. There would be an added dependency from compiler to reflect library, which
   complicates things.
*/
