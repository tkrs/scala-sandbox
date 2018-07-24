package sandbox.catbirdbox

import java.util.concurrent.{Executors, TimeUnit}

import cats.Id
import cats.data.EitherT
import cats.implicits._
import io.catbird.util._
import com.twitter.conversions.time._
import com.twitter.util._

import scala.concurrent.blocking

object Main extends App {
  //  val es = Executors.newFixedThreadPool(2)
  //  val pool = FuturePool.interruptible(es)
  //  val timer = new JavaTimer(false)

  //  def f(wait: Long): EitherT[Future, Throwable, Long] =
  //    EitherT.liftF[Future, Throwable, Long](pool {
  //      throw new Exception("fff")
  //      blocking(Thread.sleep(wait))
  //      wait
  //    }.within(timer, 600.millis))

  println(EitherT.fromEither[Id](Left(new Exception("ooo"))).value)
  println(EitherT[Id, Throwable, Int](Left(new Exception("ooo"))).value)
  println(EitherT.liftF[Try, Throwable, Int](Try(throw new Exception("aaa"))).value)

  //  val fs = Seq(
  //    f(1000).getOrElseF(Future.value(1L)),
  //    f(100).value,
  //    f(300).value,
  //    f(5000).value
  //  )
  //
  //  fs.foreach { f =>
  //    try println(Await.result(f))
  //    catch {
  //      case e: Throwable =>
  //        println(e)
  //    }
  //  }

  //  es.awaitTermination(1, TimeUnit.SECONDS)
  //  es.shutdownNow()
}
