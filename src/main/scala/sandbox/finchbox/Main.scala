package sandbox.finchbox

import cats.effect.{ContextShift, IO}
import com.twitter.finagle.Http
import com.twitter.util.Await
import io.finch._
import io.finch.circe._

import scala.concurrent.ExecutionContext

object Main extends App with Endpoint.Module[IO] {

  val foo = get(path("users") :: path("foo")).mapAsync(_ => IO(10))
  val bar = get(path("users") :: path("bar")).mapAsync(_ => IO("10"))

  val users = foo :+: bar

  implicit val S: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val index = classpathAsset("/index.html")

  val api = Bootstrap
    .serve[Application.Json](users)
    .serve[Text.Html](index)
    .toService

  Await.ready(Http.serve(":8000", api))
}
