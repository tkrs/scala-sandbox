package sandbox.monad.study

import data.{ Maybe, Just, Nothing }
import org.scalatest._

class MonadSpec extends FunSuite {

  implicit val maybeMonadLaws: MonadLaws[Maybe] = new MonadLaws[Maybe] {
    override implicit val F: Monad[Maybe] = new Monad[Maybe] {
      override def flatMap[A, B](fa: Maybe[A])(f: A => Maybe[B]): Maybe[B] =
        fa match { case Just(a) => f(a); case _ => Nothing }
      override def pure[A](a: A): Maybe[A] = Maybe(a)
    }
  }

  test("MonadLaws") {
    val f: String => Maybe[Int] =
      x => try Just(x.toInt) catch { case _: Throwable => Nothing }

    assert(MonadLaws[Maybe].leftIdentity("123", f))

    assert(MonadLaws[Maybe].rightIdentity(Maybe("123")))

    val g: Int => Maybe[String] =
      x => try Just(x.toString) catch { case _: Throwable => Nothing }

    assert(MonadLaws[Maybe].associativity(Maybe("1"), f, g))
  }

  test("for-comprehension") {

    implicit val maybeMonad: Monad[Maybe] = maybeMonadLaws.F

    import Functor._
    import Monad._

    val maybe: Maybe[Long] = for {
      x <- Maybe(10L)
      y <- Maybe(10L)
    } yield x * y

    assert(maybe === Just(100L))
  }
}
