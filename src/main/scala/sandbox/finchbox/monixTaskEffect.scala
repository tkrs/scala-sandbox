package sandbox.finchbox

import cats.StackSafeMonad
import cats.effect.{Effect, ExitCase, IO, SyncIO}
import monix.eval.Task
import monix.execution.Scheduler

object monixTaskEffect {

  implicit def monixTaskInstance(implicit S: Scheduler): Effect[Task] = new Effect[Task] with StackSafeMonad[Task] {

    override def runAsync[A](fa: Task[A])(cb: Either[Throwable, A] => IO[Unit]): SyncIO[Unit] =
      IO.async[A](fa.runAsync).runAsync(cb)

    override def async[A](k: (Either[Throwable, A] => Unit) => Unit): Task[A] =
      Task.async[A] { cb =>
        k {
          case Right(v) => cb.onSuccess(v)
          case Left(e)  => cb.onError(e)
        }
      }

    override def asyncF[A](k: (Either[Throwable, A] => Unit) => Task[Unit]): Task[A] =
      Task.asyncF[A] { cb =>
        k {
          case Right(v) => cb.onSuccess(v)
          case Left(e)  => cb.onError(e)
        }
      }

    override def suspend[A](thunk: => Task[A]): Task[A] =
      Task.suspend(thunk)

    override def bracketCase[A, B](acquire: Task[A])(use: A => Task[B])(
        release: (A, ExitCase[Throwable]) => Task[Unit]): Task[B] =
      acquire.bracketCase(use)(release)

    override def flatMap[A, B](fa: Task[A])(f: A => Task[B]): Task[B] =
      fa.flatMap(f)

    override def raiseError[A](e: Throwable): Task[A] =
      Task.raiseError(e)

    override def handleErrorWith[A](fa: Task[A])(f: Throwable => Task[A]): Task[A] =
      fa.onErrorHandleWith(f)

    override def pure[A](x: A): Task[A] =
      Task.now(x)
  }
}
