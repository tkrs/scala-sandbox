package sandbox.monad.study

import data.{ Maybe, Just, Nothing }
import org.scalatest._

class FunctorSpec extends FunSuite {

  implicit val functorLaws: FunctorLaws[Maybe] = new FunctorLaws[Maybe] {
    implicit override val F: Functor[Maybe] = new Functor[Maybe] {
      override def map[A, B](fa: Maybe[A])(f: A => B): Maybe[B] =
        fa match { case Just(a) => Just(f(a)); case _ => Nothing }
    }
  }

  test("FunctorLaws") {
    assert(FunctorLaws[Maybe].identity(Maybe(10)))
    val toString: Int => String = _.toString
    val toInt: String => Int = _.toInt
    assert(FunctorLaws[Maybe].associativity(Maybe(10), toString, toInt))
  }
}
