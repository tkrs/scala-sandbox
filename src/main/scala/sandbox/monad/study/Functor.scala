package sandbox.monad.study

import scala.Predef.{identity => id}

trait Functor[F[_]] {

  def map[A, B](fa: F[A])(f: A => B): F[B]
}

object Functor {

  def apply[F[_]](implicit F: Functor[F]): Functor[F] = F

  implicit final class FunctorOp[F[_], A](val fa: F[A]) extends AnyVal {
    @inline def `<$>`[B](f: A => B)(implicit F: Functor[F]): F[B] = map(f)

    @inline def map[B](f: A => B)(implicit F: Functor[F]): F[B] = F.map(fa)(f)
  }

}

trait FunctorLaws[F[_]] {

  import Functor._

  implicit val F: Functor[F]

  def identity[A](fa: F[A]): Boolean =
    fa `<$>` id == fa

  def associativity[A, B, C](fa: F[A], f: A => B, g: B => C): Boolean =
    (fa `<$>` f) `<$>` g == fa `<$>` (f.andThen(g))
}

object FunctorLaws {

  def apply[F[_]](implicit F: FunctorLaws[F]): FunctorLaws[F] = F
}
