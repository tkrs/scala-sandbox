package sandbox.monad.study

import scala.{Nothing => Nothing0}

object data {

  sealed trait Maybe[+A]

  final case class Just[+A](a: A) extends Maybe[A]

  final case object Nothing extends Maybe[Nothing0]

  object Maybe {
    def apply[A](a: A): Maybe[A] =
      if (a == null) Nothing else Just(a)
  }

}
