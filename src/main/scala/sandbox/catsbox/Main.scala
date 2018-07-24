package sandbox
package catsbox

import cats.{Applicative, Eval, Traverse}
import cats.free.{Cofree, Free}

sealed trait Expr[A]

object Main extends App {

  //  type Fexpr[A] = Cofree[Expr, A]
  //
  //  implicit val calTravers: Traverse[Expr] = new Traverse[Expr] {
  //    def traverse[G[_], A, B](fa: Expr[A])(f: A => G[B])(implicit ev1: Applicative[G]): G[Expr[B]] =
  //      ???
  //    def foldLeft[A, B](fa: Expr[A], b: B)(f: (B, A) => B): B                           = ???
  //    def foldRight[A, B](fa: Expr[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] = ???
  //  }
  //
  //  def f(expr: List[String]): Expr[List[String]] = expr match {
  //    case "(" +: r :+ ")"    => Fun(Cofree.unfold(r)(f))
  //    case "+" :: r :: l :: t => ???
  //    case "-" :: t           => ???
  //    case "*" :: t           => ???
  //    case "/" :: t           => ???
  //    case n :: t             => ???
  //    case otherwise          => ???
  //  }
  //
  //  val co = Cofree.unfold[Expr, List[String]]("+ 1 1".split(' ').toList)(f)
  //
  //  val x = Cofree.cata[Expr, List[String], Int](co)((a, fb) => Eval.later(10))
  //
  //  Log.println(x.value)

  import cats.instances.option._
  import cats.syntax.option._

  def gen(i: Int) =
    Cofree.ana[Option, Int, Int](i)(a => if (a <= 0) none else (a / 2).some, identity)

  def ana = gen(100)

  def r =
    Cofree.cata[Option, Int, Boolean](ana)((a, fb) =>
      Eval.later(fb.map(_ && (a % 2 == 0)).getOrElse(true)))

  Log.println(r.value)
}
