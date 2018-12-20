package sandbox.fs2box

import java.io.{ByteArrayInputStream, InputStream}
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{ExecutorService, Executors}

import cats.effect._
import cats.syntax.flatMap._
import fs2._
import sandbox.Log

import scala.concurrent._
import scala.concurrent.duration.SECONDS

object A {
  val xs                = (1 to 100000).map(_.toString).mkString("\n")
  val line: InputStream = new ByteArrayInputStream(xs.getBytes(StandardCharsets.UTF_8))
}

import A._

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val executor = Executors.newFixedThreadPool(2)
    implicit val contextShift: ContextShift[IO] =
      IO.contextShift(ExecutionContext.fromExecutorService(executor))

    val counter = new AtomicInteger(0)
    def ioStream(ec: ExecutionContext) =
      io.readInputStream(IO(line), 100, ec)
        .through(text.utf8Decode)
        .through(text.lines)
        .filter(_.nonEmpty)
        .chunkN(50)
        .mapAsync(4)(s =>
          IO {
            val c = s.toVector
            Log.println(c.size)
            c.foreach { s =>
              counter.incrementAndGet()
              // if (s == "8813") throw new Exception("Oops!")
              Log.println(s)
            }
        })

    blockingExecutionContext().use { ec =>
      ioStream(ec).compile.drain.attempt >>= {
        case Right(_) =>
          Log.println(counter.get)
          shutdownAndTerminate(executor) >> IO.pure(ExitCode.Success)
        case Left(e) =>
          Log.println(e.toString)
          e.printStackTrace()
          IO.pure(ExitCode.Error)
      }
    }
  }

  private def blockingExecutionContext(): Resource[IO, ExecutionContext] =
    Resource(IO {
      val executor             = Executors.newCachedThreadPool()
      val ec: ExecutionContext = ExecutionContext.fromExecutor(executor)
      (ec, shutdownAndTerminate(executor))
    })

  private def shutdownAndTerminate(pool: ExecutorService): IO[Unit] = {
//    def await      = IO(pool.awaitTermination(30, SECONDS))
//    def raiseError = IO.raiseError[Unit](new Exception("Pool didn't terminate"))
//    def recover    = IO(pool.shutdownNow()) >> await.ifM(IO.unit, raiseError)
//
//    IO(pool.shutdown()) >> await.ifM(IO.unit, recover)
    IO(pool.shutdown())
  }
}
