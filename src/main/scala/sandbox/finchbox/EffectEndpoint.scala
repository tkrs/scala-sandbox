package sandbox.finchbox

import cats.effect.Effect
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import io.finch._
import io.finch.circe._

abstract class EffectEndpoint[F[_]](implicit F: Effect[F]) extends EndpointModule[F] {

  private[this] val foo =
    get(path("users") :: path("foo") :: path[Int]).mapAsync(a => F.delay(a * 10))
  private[this] val bar =
    get(path("users") :: path("bar") :: path[Int]).mapAsync(a => F.delay(s"$a * 10"))
  private[this] val quux =
    get(path("users") :: path("quux") :: path[Int]).mapAsync(a => F.delay(a * 10.0))

  private[this] val users = foo :+: bar :+: quux

  val api: Service[Request, Response] = Bootstrap
    .serve[Application.Json](users)
    .toService

}
