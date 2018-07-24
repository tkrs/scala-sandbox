package sandbox.monad.study

trait Monad[F[_]] extends Applicative[F] {
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

  def pure[A](a: A): F[A]

  override def ap[A, B](ff: F[A => B])(fa: F[A]): F[B] =
    flatMap(ff)(map(fa))

  override def map[A, B](fa: F[A])(f: (A) => B): F[B] =
    flatMap(fa)(a => pure(f(a)))

  def liftM2[A, B, C](fa: F[A])(fb: F[B])(f: (A, B) => C): F[C] =
    flatMap(fa)(a => flatMap(fb)(b => pure(f(a, b))))
}

object Monad {

  implicit final class MonadOp[F[_], A](val fa: F[A]) extends AnyVal {
    def >>=[B](f: A => F[B])(implicit F: Monad[F]): F[B] =
      flatMap(f)

    def flatMap[B](f: A => F[B])(implicit F: Monad[F]): F[B] =
      F.flatMap(fa)(f)

    def >>[B](fb: F[B])(implicit F: Monad[F]): F[B] =
      flatMap(_ => fb)
  }

}

trait MonadLaws[F[_]] {

  import Monad._, Applicative._

  implicit val F: Monad[F]

  def leftIdentity[A, B](a: A, f: A => F[B]): Boolean =
    (a.pure >>= f) == f(a)

  def rightIdentity[A, B](fa: F[A]): Boolean =
    (fa >>= F.pure) == fa

  def associativity[A, B, C](fa: F[A], f: A => F[B], g: B => F[C]): Boolean =
    ((fa >>= f) >>= g) == (fa >>= (x => f(x) >>= g))
}

object MonadLaws {

  def apply[F[_]](implicit F: MonadLaws[F]): MonadLaws[F] = F
}
