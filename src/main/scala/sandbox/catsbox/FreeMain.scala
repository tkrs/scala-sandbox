package sandbox.catsbox

import cats.~>
import cats.syntax.option._
import cats.free.Free
import sandbox.Log

sealed trait F[A]
object F {
  final case class X[A](a: A, f: ToInt[A])     extends F[Int]
  final case class Y(a: Int)                   extends F[Int]
  final case class Z[A](a: Int, f: FromInt[A]) extends F[A]

  def x[A](a: A)(implicit A: ToInt[A]): Free[F, Int]   = Free.liftF[F, Int](X(a, A))
  def y(a: Int): Free[F, Int]                          = Free.liftF[F, Int](Y(a))
  def z[A](a: Int)(implicit A: FromInt[A]): Free[F, A] = Free.liftF[F, A](Z(a, A))

  def interp[A]: F ~> Option = λ[F ~> Option] {
    case X(a, f) => f(a)
    case Y(a)    => a.some
    case Z(a, f) => f(a)
  }
}

trait ToInt[A] {
  def apply(a: A): Option[Int]
}
trait FromInt[A] {
  def apply(a: Int): Option[A]
}

final case class G(v: String)
object G {
  implicit val toIntG: ToInt[G] = _.v.toInt.some
}
final case class H(v: Int)
object H {
  implicit val fromIntH: FromInt[H] = v => H(v).some
}

object FreeMain extends App {

  val r = for {
    xx <- F.x[G](G("1"))
    yy <- F.y(2)
    zz <- F.z[H](3)
  } yield xx * yy * zz.v

  Log.println(r)
}
