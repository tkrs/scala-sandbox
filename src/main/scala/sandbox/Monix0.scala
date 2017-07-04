package sandbox

import java.time.Instant
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.util.concurrent._
import java.util.concurrent.atomic.AtomicLong

import cats.data.Kleisli
import cats.syntax.cartesian._
import com.google.common.util.concurrent._
import monix.cats._
import monix.eval.{Callback, Task}
import monix.execution.{Ack, Cancelable, Scheduler}
import monix.reactive.{Consumer, Observable, Observer}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future, Promise}

object Mo {

  private[this] val dtf: DateTimeFormatter = new DateTimeFormatterBuilder().appendInstant(3).toFormatter

  def prn(s: String): Unit =
    println(s"${dtf.format(Instant.now)} [${Thread.currentThread().getName}] - $s.")
}


// Conversion from some Java Futures
object Monix0 {

  // implicit val scheduler: Scheduler = Scheduler.computation(daemonic = false)
  implicit val scheduler: Scheduler = Scheduler.Implicits.global

  val service: ListeningExecutorService =
    MoreExecutors.listeningDecorator(Executors.newWorkStealingPool())

  def sleep(): Unit = TimeUnit.MILLISECONDS.sleep(1)


  def f: ListenableFuture[Int] = service.submit(() => {
    sleep()
    1000000000
  })

  def async: Task[Int] = {
    val p = Promise[Int]
    Futures.addCallback(f, new FutureCallback[Int] {
      override def onFailure(t: Throwable): Unit = p.failure(t)
      override def onSuccess(result: Int): Unit = p.success(result)
    }, MoreExecutors.directExecutor())
    Task.fromFuture(p.future)
  }

  def foo: (Int, Int) = {
    val fa = async
    val fb = async

    Await.result((fa |@| fb).tupled.runAsync, 10.seconds)
  }

  def bar: (Int, Int) = {
    val fa: Kleisli[Task, Int, Int] = Kleisli.lift(async)
    val fb: Kleisli[Task, Int, Int] = Kleisli.lift(async)

    Await.result((fa |@| fb).tupled.run(1).runAsync, 10.seconds)
  }

  def qux: (Int, Int) = {
    val fa: Kleisli[Task, Int, Int] = Kleisli.lift(Task.fork(async))
    val fb: Kleisli[Task, Int, Int] = Kleisli.lift(Task.fork(async))

    Await.result((fa |@| fb).tupled.run(1).runAsync, 10.seconds)
  }

  def t = Task {
    sleep()
    1000000000
  }

  def hoge: (Int, Int) = {
    val fa = t
    val fb = t

    Await.result((fa |@| fb).tupled.runAsync, 10.seconds)
  }

  def fuga: (Int, Int) = {
    val fa: Kleisli[Task, Int, Int] = Kleisli { _ => t }
    val fb: Kleisli[Task, Int, Int] = Kleisli { _ => t }

    Await.result((fa |@| fb).tupled.run(1).runAsync, 10.seconds)
  }
  def watch(name: String, f: => Unit): Unit = {
    val start = System.nanoTime()
    println(f)
    println(s"$name elapsed: ${TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)} ms")
  }

  def main(args: Array[String]): Unit = {
    watch("foo", foo)
    watch("bar", bar)
    watch("qux", qux)
    watch("hoge", hoge)
    watch("fuga", fuga)
    service.shutdownNow()
  }
}

// Subscriber
object Monix1 {
  implicit val scheduler: Scheduler = Scheduler.computation(daemonic = false)

  def sub(observable: Observable[String])(implicit executor: ExecutionContext): Cancelable =
    observable.subscribe(new Observer[String] {
      override def onError(ex: Throwable): Unit = println(s"onError: ${ex.toString}")

      override def onComplete(): Unit = println("onComplete")

      override def onNext(elem: String): concurrent.Future[Ack] = {
        if (elem == "BYE") Ack.Stop
        else for {
          _ <- Future { Thread.sleep(50); Mo.prn(s"$elem: f1") }
          _ <- Future { Thread.sleep(50); Mo.prn(s"$elem: f2")}
          _ <- Future { Thread.sleep(50); Mo.prn(s"$elem: f3")}
          c <- Ack.Continue
        } yield c
      }
    })

  def main(args: Array[String]): Unit = {

    val queue = new ConcurrentLinkedQueue[String]
    (1 to 100).foreach(a => queue.offer(a.toString))

    val observable = Observable.repeatEval(Option(queue.poll())).map({
      case Some(v) => v
      case _ => "BYE"
    })
    sub(observable)(ExecutionContext.fromExecutorService(Executors.newWorkStealingPool()))
  }
}

// Consumer with parallel
object Monix2 {
  implicit val scheduler: Scheduler = Scheduler.computation(daemonic = false)

  def consume(observable: Observable[String]): Task[Unit] = {

    val consumer = Consumer.foreachParallelAsync[String](8) { s =>
      for {
        _ <- Task { Thread.sleep(50); Mo.prn(s"$s: t1")}
        _ <- Task { Thread.sleep(50); Mo.prn(s"$s: t2")}
        _ <- Task { Thread.sleep(50); Mo.prn(s"$s: t3")}
      } yield ()
    }

    val balanced = Consumer.loadBalance(consumer)
    observable.consumeWith(balanced).map(_ => ())
  }

  def main(args: Array[String]): Unit = {

    val queue = new ConcurrentLinkedQueue[String]
    (1 to 100).foreach(a => queue.offer(a.toString))

    val observable = Observable.repeatEval(Option(queue.poll())).collect({ case Some(v) => v })

    consume(observable).runAsync(new Callback[Unit] {
      override def onSuccess(value: Unit): Unit = Mo.prn("completed")
      override def onError(ex: Throwable): Unit = ex.printStackTrace()
    })
  }
}

// Gather
object Monix3 {
  // implicit val scheduler: Scheduler = Scheduler.computation(daemonic = false)
  val scheduler: Scheduler = Scheduler.singleThread(name = "mo", daemonic = false)

  def tasks(s: String): Task[Unit] = for {
    _ <- Task { Thread.sleep(50); Mo.prn(s"$s: t1")}
    _ <- Task { Thread.sleep(50); Mo.prn(s"$s: t2")}
    _ <- Task { Thread.sleep(50); Mo.prn(s"$s: t3")}
  } yield ()

  def main(args: Array[String]): Unit = {
    implicit val ctx = ExecutionContext.fromExecutorService(Executors.newWorkStealingPool(3))

    val queue = new ConcurrentLinkedQueue[String]
    (1 to 1000).foreach(a => queue.offer(a.toString))

    def fn = Future {
      val iter =
        Iterator.continually(queue.poll())
          .takeWhile { v => Mo.prn(s"input value: $v"); v != null }
          .map(tasks)
          .take(100)

      Task.gatherUnordered(iter).runAsync(new Callback[List[Unit]] {
        override def onError(ex: Throwable): Unit = ex.printStackTrace()

        override def onSuccess(value: List[Unit]): Unit = Mo.prn("complete")
      })(scheduler)

    }

    import cats.instances.future._
    import cats.syntax.cartesian._

    Await.result(
      (fn |@| fn |@| fn |@| fn |@| fn |@| fn |@| fn |@| fn |@| fn |@| fn |@| fn).tupled,
      100.seconds
    )
  }
}


object Iter {

  implicit val scheduler: Scheduler = Scheduler.singleThread(name = "mo", daemonic = false)

  def main(args: Array[String]): Unit = {
    val counter = new AtomicLong(0)
    def iter = Iterator.continually(counter.getAndIncrement()).map{ a => println(s"value: $a"); Task(a) }.take(100)
    iter.foreach(println)

    Task.gatherUnordered[Long](iter).foreach(println)
  }
}
