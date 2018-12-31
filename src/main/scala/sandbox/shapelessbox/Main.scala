package sandbox.shapelessbox

import sandbox.Log
import shapeless.labelled.FieldType
import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Witness}

final case class Test(a: Int, b: String)
object Test {}

sealed trait Ast { self =>
  import Ast._
  final def fold[A](foo: Foo => A, bar: Bar => A, qux: Qux => A, empty: => A): A = self match {
    case x @ Foo(_)       => foo(x)
    case x @ Bar(_)       => bar(x)
    case x @ Qux(_, _, _) => qux(x)
    case Empty            => empty
  }
}
object Ast {
  final case class Foo(a: Int)                              extends Ast
  final case class Bar(a: String)                           extends Ast
  final case class Qux(name: String, value: Ast, tail: Ast) extends Ast
  final case object Empty                                   extends Ast
}

trait Encoder[A] {
  def apply(a: A): Ast
}
object Encoder {
  import Ast._

  def apply[A](implicit A: Encoder[A]): Encoder[A] = A

  implicit val encodeInt: Encoder[Int] = new Encoder[Int] {
    def apply(a: Int): Ast = Foo(a)
  }

  implicit val encodeString: Encoder[String] = new Encoder[String] {
    def apply(a: String): Ast = Bar(a)
  }

  implicit val encodeHNil: Encoder[HNil] = new Encoder[HNil] {
    def apply(a: HNil): Ast = Empty
  }

  implicit def encodeHCons[K <: Symbol, H, T <: HList](implicit
                                                       wk: Witness.Aux[K],
                                                       encodeH: Encoder[H],
                                                       encodeT: Encoder[T]): Encoder[FieldType[K, H] :: T] =
    new Encoder[FieldType[K, H] :: T] {
      def apply(a: FieldType[K, H] :: T): Ast =
        Qux(wk.value.name, encodeH(a.head), encodeT(a.tail))
    }

  implicit def encodeGen[A, R](implicit
                               lgen: LabelledGeneric.Aux[A, R],
                               encodeR: Lazy[Encoder[R]]): Encoder[A] = new Encoder[A] {
    def apply(a: A): Ast = encodeR.value.apply(lgen.to(a))
  }
}

object Main extends App {
  Log.println(Encoder[Test].apply(Test(10, "hello")))
}
