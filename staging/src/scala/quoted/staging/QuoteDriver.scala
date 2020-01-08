package scala.quoted
package staging

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.Driver
import dotty.tools.dotc.core.Contexts.{Context, ContextBase, FreshContext}
import dotty.tools.dotc.tastyreflect.ReflectionImpl
import dotty.tools.io.{AbstractFile, Directory, PlainDirectory, VirtualDirectory}
import dotty.tools.repl.AbstractFileClassLoader
import dotty.tools.dotc.reporting._
import scala.quoted._
import scala.quoted.staging.Toolbox
import java.net.URLClassLoader

/** Driver to compile quoted code
 *
 * @param appClassloader classloader of the application that generated the quotes
 */
private class QuoteDriver(appClassloader: ClassLoader) extends Driver {
  import tpd._

  private[this] val contextBase: ContextBase = new ContextBase

  def run[T](exprBuilder: QuoteContext => Expr[T], settings: Toolbox.Settings): T = {
    val outDir: AbstractFile = settings.outDir match {
      case Some(out) =>
        val dir = Directory(out)
        dir.createDirectory()
        new PlainDirectory(Directory(out))
      case None =>
        new VirtualDirectory("<quote compilation output>")
    }

    val (_, ctx0: Context) = setup(settings.compilerArgs.toArray :+ "dummy.scala", initCtx.fresh)
    val ctx = setToolboxSettings(ctx0.fresh.setSetting(ctx0.settings.outputDir, outDir), settings)

    new QuoteCompiler().newRun(ctx).compileExpr(exprBuilder) match {
      case Right(value) =>
        value.asInstanceOf[T]

      case Left(classname) =>
        assert(!ctx.reporter.hasErrors)

        val classLoader = new AbstractFileClassLoader(outDir, appClassloader)

        val clazz = classLoader.loadClass(classname)
        val method = clazz.getMethod("apply")
        val inst = clazz.getConstructor().newInstance()

        method.invoke(inst).asInstanceOf[T]
    }
  }

  override def initCtx: Context = {
    val ictx = contextBase.initialCtx
    ictx.settings.classpath.update(getCurrentClasspath(appClassloader))(ictx)
    ictx
  }

  private def setToolboxSettings(ctx: FreshContext, settings: Toolbox.Settings): ctx.type = {
    ctx.setSetting(ctx.settings.YshowRawQuoteTrees, settings.showRawTree)
    // An error in the generated code is a bug in the compiler
    // Setting the throwing reporter however will report any exception
    ctx.setReporter(new ThrowingReporter(ctx.reporter))
  }

  private def getCurrentClasspath(cl: ClassLoader): String = {
    val classpath0 = System.getProperty("java.class.path")
    cl match {
      case cl: URLClassLoader =>
        // Loads the classes loaded by this class loader
        // When executing `run` or `test` in sbt the classpath is not in the property java.class.path
        import java.nio.file.Paths
        val newClasspath = cl.getURLs.map(url => Paths.get(url.toURI).toString)
        newClasspath.mkString("", java.io.File.pathSeparator, if (classpath0 == "") "" else java.io.File.pathSeparator + classpath0)
      case _ => classpath0
    }
  }
}

