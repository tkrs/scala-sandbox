package sandbox.monixbox

import cats.syntax.apply._
import monix.eval.Task

object Main extends App {
  def f(i: Int): Task[Unit] = {
    if (i == 0) Task.unit
    else Task.unit *> f(i - 1)
  }

  f(6000)
}
