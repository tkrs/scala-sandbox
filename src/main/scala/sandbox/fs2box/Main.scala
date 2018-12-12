package sandbox.fs2box

import java.io.{ByteArrayInputStream, InputStream}
import java.nio.charset.StandardCharsets
import java.util.concurrent.{Executors, ThreadFactory}

import cats.effect.{ExitCode, IO, IOApp}
import fs2._

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

object A {
  val line: InputStream = new ByteArrayInputStream(s"""
       |red
       |red
       |blue
       |yellow
       |yellow
       |red
       |red
       |red
       |yellow
       |green
       |blue
       |""".stripMargin.getBytes(StandardCharsets.UTF_8))
}

import A._

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val ec = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool(new ThreadFactory {
      override def newThread(r: Runnable): Thread = {
        val t = new Thread(r)
        t.setDaemon(true)
        t
      }
    }))
    io.readInputStream(IO(line), 3, ec)
      .through(text.utf8Decode)
      .through(text.lines)
      .map(s => println(s))
      .compile
      .drain
      .map(_ => ExitCode.Success)
      .handleErrorWith {
        case NonFatal(_) => IO.pure(ExitCode.Error)
      }
  }
}
