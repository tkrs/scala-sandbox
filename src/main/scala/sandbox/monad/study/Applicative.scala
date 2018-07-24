package sandbox.monad.study

import scala.Predef.{identity => id}

trait Applicative[F[_]] extends Functor[F] {
  def ap[A, B](ff: F[A => B])(fa: F[A]): F[B]

  def pure[A](a: A): F[A]

  override def map[A, B](fa: F[A])(f: A => B): F[B] = ap(pure(f))(fa)
}

object Applicative {

  implicit final class ApplicativePureOp[A](val a: A) extends AnyVal {
    @inline def pure[F[_]](implicit F: Applicative[F]): F[A] = F.pure(a)
  }

  implicit final class ApplicativeOp[F[_], A, B](val ff: F[A => B]) extends AnyVal {
    @inline def <*>(fa: F[A])(implicit F: Applicative[F]): F[B] = F.ap(ff)(fa)
  }

}

trait ApplicativeLaws[F[_]] {

  import Applicative._

  implicit val F: Applicative[F]

  def identity[A](fa: F[A]): Boolean =
    (id[A] _).pure <*> fa == fa

  def homomorphism[A, B](a: A, f: A => B): Boolean =
    f.pure <*> a.pure == f(a).pure

  def interchange[A, B](a: A, ff: F[A => B]): Boolean =
    ff <*> a.pure == ((f: A => B) => f(a)).pure <*> ff
}

object ApplicativeLaws {

  def apply[F[_]](implicit F: ApplicativeLaws[F]): ApplicativeLaws[F] = F
}
