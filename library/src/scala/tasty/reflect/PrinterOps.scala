package scala.tasty
package reflect

import scala.quoted.show.SyntaxHighlight

trait PrinterOps extends Core { self: Reflection =>

  /** Adds `show` as an extension method of a `Tree` */
  implicit class TreeShowDeco(tree: Tree) {
    /** Shows the tree as extractors */
    def showExtractors(given ctx: Context): String =
      new ExtractorsPrinter[self.type](self).showTree(tree)

    /** Shows the tree as fully typed source code */
    def show(given ctx: Context): String =
      show(SyntaxHighlight.plain)

    /** Shows the tree as fully typed source code */
    def show(syntaxHighlight: SyntaxHighlight)(given ctx: Context): String =
      new SourceCodePrinter[self.type](self)(syntaxHighlight).showTree(tree)

  }

  /** Adds `show` as an extension method of a `TypeOrBounds` */
  implicit class TypeOrBoundsShowDeco(tpe: TypeOrBounds) {
    /** Shows the tree as extractors */
    def showExtractors(given ctx: Context): String =
      new ExtractorsPrinter[self.type](self).showTypeOrBounds(tpe)

    /** Shows the tree as fully typed source code */
    def show(given ctx: Context): String =
      show(SyntaxHighlight.plain)

    /** Shows the tree as fully typed source code */
    def show(syntaxHighlight: SyntaxHighlight)(given ctx: Context): String =
      new SourceCodePrinter[self.type](self)(syntaxHighlight).showTypeOrBounds(tpe)
  }

  /** Adds `show` as an extension method of a `Constant` */
  implicit class ConstantShowDeco(const: Constant) {
    /** Shows the tree as extractors */
    def showExtractors(given ctx: Context): String =
      new ExtractorsPrinter[self.type](self).showConstant(const)

    /** Shows the tree as fully typed source code */
    def show(given ctx: Context): String = show(SyntaxHighlight.plain)

    /** Shows the tree as fully typed source code */
    def show(syntaxHighlight: SyntaxHighlight)(given ctx: Context): String =
      new SourceCodePrinter[self.type](self)(syntaxHighlight).showConstant(const)
  }

  /** Adds `show` as an extension method of a `Symbol` */
  implicit class SymbolShowDeco(symbol: Symbol) {
    /** Shows the tree as extractors */
    def showExtractors(given ctx: Context): String =
      new ExtractorsPrinter[self.type](self).showSymbol(symbol)

    /** Shows the tree as fully typed source code */
    def show(given ctx: Context): String = show(SyntaxHighlight.plain)

    /** Shows the tree as fully typed source code */
    def show(syntaxHighlight: SyntaxHighlight)(given ctx: Context): String =
      new SourceCodePrinter[self.type](self)(syntaxHighlight).showSymbol(symbol)
  }

  /** Adds `show` as an extension method of a `Flags` */
  implicit class FlagsShowDeco(flags: Flags) {
    /** Shows the tree as extractors */
    def showExtractors(given ctx: Context): String =
      new ExtractorsPrinter[self.type](self).showFlags(flags)

    /** Shows the tree as fully typed source code */
    def show(given ctx: Context): String = show(SyntaxHighlight.plain)

    /** Shows the tree as fully typed source code */
    def show(syntaxHighlight: SyntaxHighlight)(given ctx: Context): String =
      new SourceCodePrinter[self.type](self)(syntaxHighlight).showFlags(flags)
  }


}
