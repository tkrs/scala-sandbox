package sandbox.finchbox

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.Service
import com.twitter.util.Await
import io.finch._
import io.finch.circe._
import org.openjdk.jmh.annotations._

import scala.concurrent.duration._

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(SECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(2)
abstract class RootBench

//class FinchBench extends RootBench {
//  import io.finch.syntax._
//  import com.twitter.util.Future
//
//  private[this] val foo  = get(path("users") :: path("foo")).mapAsync(_ => Future(10))
//  private[this] val bar  = get(path("users") :: path("bar")).mapAsync(_ => Future("10"))
//  private[this] val quux = get(path("users") :: path("quux")).mapAsync(_ => Future(10.0))
//
//  private[this] val users = foo :+: bar :+: quux
//
////  private[this] val index = get("/index.html").map(_ => "index")
//
//  private[this] val api: Service[Request, Response] = Bootstrap
//    .serve[Application.Json](users)
//    //.serve[Text.Html](index)
//    .toService
//
//  @Benchmark
//  def requestBench: Response = {
//    Await.result(api.apply(Request("/users/bar")))
//  }
//}

class FinchCatsEffectBench extends RootBench {
  import cats.effect.{ContextShift, IO}
  import io.finch.catsEffect._

  import scala.concurrent.ExecutionContext

  implicit val S: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  private[this] val foo  = get(path("users") :: path("foo")).mapAsync(_ => IO(10))
  private[this] val bar  = get(path("users") :: path("bar")).mapAsync(_ => IO("10"))
  private[this] val quux = get(path("users") :: path("quux")).mapAsync(_ => IO(10.0))

  private[this] val users = foo :+: bar :+: quux

//  private[this] val index = classpathAsset("/index.html")

  private[this] val api: Service[Request, Response] = Bootstrap
    .serve[Application.Json](users)
//    .serve[Text.Html](index)
    .toService

  @Benchmark
  def requestBench: Response = {
    Await.result(api.apply(Request("/users/bar")))
  }
}
