package sandbox.finchbox

import cats.StackSafeMonad
import cats.effect.{Effect, ExitCase, IO, SyncIO}
import com.twitter.util.{Future, Promise, Return, Throw}

object futureEffect {

  implicit def twitterFutureInstance: Effect[Future] = new Effect[Future] with StackSafeMonad[Future] {

    override def runAsync[A](fa: Future[A])(cb: Either[Throwable, A] => IO[Unit]): SyncIO[Unit] =
      IO.async[A](cb =>
          fa.respond {
            case Return(r) => cb(Right(r))
            case Throw(e)  => cb(Left(e))
        })
        .runAsync(cb)

    override def async[A](k: (Either[Throwable, A] => Unit) => Unit): Future[A] = {
      val p = Promise[A]

      k { e =>
        if (p.isDefined) ()
        else
          e match {
            case Right(v) => p.setValue(v)
            case Left(e)  => p.setException(e)
          }
      }

      p
    }

    override def asyncF[A](k: (Either[Throwable, A] => Unit) => Future[Unit]): Future[A] = {
      val p = Promise[A]

      val t = k { r =>
        if (p.isDefined) ()
        else
          r match {
            case Right(v) => p.setValue(v)
            case Left(e)  => p.setException(e)
          }
      }

      t.flatMap(_ => p)
    }

    override def suspend[A](thunk: => Future[A]): Future[A] =
      Future(thunk).flatMap(identity)

    override def bracketCase[A, B](acquire: Future[A])(use: A => Future[B])(
        release: (A, ExitCase[Throwable]) => Future[Unit]): Future[B] =
      acquire.flatMap(a => {
        val f = use(a)
        f.transform {
          case Return(_) => release(a, ExitCase.complete).flatMap(_ => f)
          case Throw(e)  => release(a, ExitCase.error(e)).flatMap(_ => f)
        }
      })

    override def flatMap[A, B](fa: Future[A])(f: A => Future[B]): Future[B] =
      fa.flatMap(f)

    override def raiseError[A](e: Throwable): Future[A] =
      Future.exception(e)

    override def handleErrorWith[A](fa: Future[A])(f: Throwable => Future[A]): Future[A] =
      fa.rescue { case e => f(e) }

    override def pure[A](x: A): Future[A] =
      Future.value(x)
  }
}
