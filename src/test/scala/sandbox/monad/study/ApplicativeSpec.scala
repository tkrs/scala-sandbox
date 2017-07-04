package sandbox.monad.study

import data.{ Maybe, Just, Nothing }
import org.scalatest._

class ApplicativeSpec extends FunSuite {

  implicit val maybeApplicativeLaws = new ApplicativeLaws[Maybe] {
    override implicit val F = new Applicative[Maybe] {
      override def ap[A, B](ff: Maybe[A => B])(fa: Maybe[A]): Maybe[B] = ff match {
        case Nothing => Nothing
        case Just(f) => fa match {
          case Nothing => Nothing
          case Just(a) => Maybe(f(a))
        }
      }
      override def pure[A](a: A): Maybe[A] = Maybe(a)
    }
  }

  test("ApplicativeLaws") {
    assert(ApplicativeLaws[Maybe].identity(Maybe(10)))

    val toInt: String => Int = _.toInt

    assert(ApplicativeLaws[Maybe].homomorphism("10", toInt))

    val toIntOption: Maybe[String => Int] = Maybe((s: String) => s.toInt)

    assert(ApplicativeLaws[Maybe].interchange("10", toIntOption))
  }
}
