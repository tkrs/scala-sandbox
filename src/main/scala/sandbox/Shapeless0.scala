package sandbox

import shapeless.Witness

object T {
  type A = Witness.`"a"`.T
  type B = Witness.`"b"`.T
}


sealed trait Enc[A <: String, B <: String, C] {
  def apply(a: A, b: B): C
}

object Enc {

  implicit val aaa: Enc[T.A, T.B, Int] = new Enc[T.A, T.B, Int] {
    def apply(a: T.A, b: T.B): Int = 10
  }
}

object Main {

  def foo[A <: String, B<:String, C](
    implicit
    C: Enc[A, B, C],
    wa: Witness.Aux[A],
    wb: Witness.Aux[B]
  ): Unit = println(C(wa.value, wb.value))

  def main(args: Array[String]): Unit = {
    implicit val wa = Witness.mkWitness(args(0))
    implicit val wb = Witness.mkWitness(args(1))

    foo
  }
}
