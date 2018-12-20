package sandbox.threading

import java.util.concurrent._
import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}

import sandbox.Log

import scala.concurrent.duration._

object Main {

  val queue = new ConcurrentLinkedQueue[Int]()

  object W extends Callable[Unit] {
    private[this] val worker  = T.scheduled("worker")
    private[this] val running = new AtomicBoolean(false)

    @volatile private var closed: Boolean = false

    def isClosed: Boolean     = closed
    def isTerminated: Boolean = worker.isTerminated

    def call(): Unit =
      if (queue.isEmpty) {
        running.set(false)
      } else {
        val xs = (1 to 100).map(_ => queue.poll()).toList

        if (queue.isEmpty) {
          running.set(false)
        } else {
          if (closed) Log.println(xs.headOption)
          val scheduler = T.scheduled("scheduler", daemon = true)
          scheduler.schedule(W, 500, MILLISECONDS)
        }
      }

    def start(): Unit =
      if (!running.get && running.compareAndSet(false, true)) {
        if (!worker.isShutdown) {
          Log.println("Start schedule.")
          worker.schedule(W, 500, MILLISECONDS)
        } else {
          running.set(false)
        }
      }

    def close(): Unit = {
      start()
      closed = true
      try T.quit(worker)
      finally {
        Log.println("Close scheduler immediately.")
      }
    }
  }

  def main(args: Array[String]): Unit = {
    Log.println("Start.")

    val task = new Runnable {
      def run(): Unit = {
        while (!W.isClosed) {
          val random = ThreadLocalRandom.current()
          val n      = random.nextInt(100, 500)
          queue.offer(n)
          W.start()
          MICROSECONDS.sleep(n)
        }
        Log.println("Task done.")
      }
    }

    val tasks = Seq(
      new Thread(task),
      new Thread(task),
      new Thread(task),
    )
    tasks.foreach(_.start())

    Log.println(s"Q1: ${queue.size}")

    SECONDS.sleep(10)

    Log.println(s"Q2: ${queue.size}")

    W.close()

    Log.println(s"Q3: ${queue.size}")

    tasks.foreach(_.join())

    Log.println(s"Q4: ${queue.size}")

    Log.println("Done.")
  }
}

object T {
  private def ntf(name: String, isDaemon: Boolean = false): ThreadFactory =
    (r: Runnable) => {
      val t = new Thread(r)
      Log.println(s"newThread($name, $isDaemon): ${t.getId}.")
      t.setName(name)
      t.setDaemon(isDaemon)
      if (t.getPriority != Thread.NORM_PRIORITY) t.setPriority(Thread.NORM_PRIORITY)
      t
    }

  def scheduled(name: String, core: Int = 1, daemon: Boolean = false): ScheduledExecutorService =
    Executors.newScheduledThreadPool(core, ntf("worker"))

  def quit(t: ExecutorService): Unit = {
    Log.println("shutdown.")
    t.shutdown()
    try {
      Log.println("awaitTermination.")
      if (!t.awaitTermination(10, SECONDS)) {
        Log.println("shutdownNow.")
        t.shutdownNow()
        Log.println("awaitTermination2.")
        if (!t.awaitTermination(10, SECONDS)) {
          throw new Exception("panic.")
        }
      }
    } catch {
      case _: InterruptedException =>
        t.shutdownNow()
        Thread.currentThread().interrupt()
    }
  }
}

object X {
  def main(args: Array[String]): Unit = {
    val w1 = T.scheduled("worker1")
    val w2 = T.scheduled("worker2")

    val q       = new ConcurrentLinkedQueue[Int]()
    val running = new AtomicBoolean(false)

    @volatile var closed: Boolean = false

    def consume(): Unit = {
      val xs = Iterator
        .continually(Option(q.poll()))
        .takeWhile(_.isDefined)
        .map(_.get)
        .take(500)
        .toVector

      Log.println(s"Size: ${xs.size}, Head: ${xs.headOption}")
      Log.println(s"Remaining: ${q.size()}")
    }

    def toggle = running.compareAndSet(false, true)

    def start(): Unit =
      if (!closed && !q.isEmpty && (!running.get && toggle)) {

        w1.execute(new Runnable { self =>
          def run(): Unit =
            w2.schedule(
              new Runnable {
                def run(): Unit = {
                  Log.println("Run task.")

                  consume()

                  if (q.isEmpty) {
                    running.set(false)
                  } else {
                    Log.println("Schedule next task.")
                    val _ = w2.schedule(this, 1, SECONDS)
                  }
                }
              },
              1,
              SECONDS
            )
        })
      }

    val task = new Runnable {
      def run(): Unit =
        try {
          while (true) {
            val random = ThreadLocalRandom.current()
            val n      = random.nextInt(1, 10)
            q.offer(n)
            start()
            MILLISECONDS.sleep(n)
          }
          Log.println("Task done.")
        } catch {
          case e: InterruptedException =>
            Log.println(e.toString)
        }
    }

    val tasks = Seq(
      new Thread(task),
      new Thread(task),
      new Thread(task),
    )
    tasks.foreach(_.setDaemon(true))
    tasks.foreach(_.start())

    val e = new Thread(() => {
      SECONDS.sleep(10)
      Log.println("Worker 1 close.")
      closed = true
      T.quit(w1)
      Log.println("Worker 2 close.")
    })
    e.start()

    e.join()

    T.quit(w2)
  }
}

object Y {
  def main(args: Array[String]): Unit = {
    val scheduler = T.scheduled("scheduler")
    val counter   = new AtomicLong(0)

    scheduler.execute(new Runnable {
      def run(): Unit =
        while (true) {
          Log.println(s"${counter.getAndIncrement()}")
          SECONDS.sleep(1)
        }
    })

    T.quit(scheduler)
  }
}
