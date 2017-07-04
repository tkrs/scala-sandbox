package benchmark

import java.util.concurrent._

// import cats.Applicative
import com.google.common.util.concurrent._
// import cats.syntax.cartesian._
import sandbox.Monix0
// import monix.cats._
import monix.eval.Task
import monix.execution.Cancelable
import org.openjdk.jmh.annotations._

//import scala.concurrent.Await
//import scala.concurrent.duration._

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
class Bench {

//  import monix.cats._
//  import Monix0.scheduler

//  implicit val applicativeTask: Applicative[Task] = new Applicative[Task] {
//    override def pure[A](x: A): Task[A] = Task(x)
//    override def ap[A, B](ff: Task[(A) => B])(fa: Task[A]): Task[B] = Task.mapBoth(ff, fa)(_(_))
//  }

  def async: Task[Int] = Task.async { (_, cb) =>
    Futures.addCallback(Monix0.f, new FutureCallback[Int] {
      override def onFailure(t: Throwable): Unit = cb.onError(t)
      override def onSuccess(result: Int): Unit = cb.onSuccess(result)
    }, MoreExecutors.directExecutor())
    Cancelable()
  }

  @TearDown
  def tearDown(): Unit =
    Monix0.service.shutdownNow()

  @Benchmark
  def foo(): (Int, Int) =
    Monix0.foo

  @Benchmark
  def bar(): (Int, Int) = Monix0.bar

  @Benchmark
  def hoge(): (Int, Int) = Monix0.hoge

  @Benchmark
  def fuga(): (Int, Int) = Monix0.fuga
}
