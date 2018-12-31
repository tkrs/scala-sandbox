package sandbox.finchbox

import com.twitter.finagle.http.{Request, Response}
import com.twitter.util._
import monix.eval.Task
import org.openjdk.jmh.annotations._

import scala.concurrent.duration._

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(SECONDS)
//@BenchmarkMode(Array(Mode.AverageTime))
//@OutputTimeUnit(NANOSECONDS)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 5)
@Fork(2)
abstract class Bench

class FinchCatsIOBench extends Bench {
  import ioEndpoint._

  @Benchmark
  def requestBench: Response =
    Await.result(api.apply(Request("/users/bar/10")))
}

class FinchRerunnableBench extends Bench {
  import rerunnableEndpoint._

  @Benchmark
  def requestBench: Response =
    Await.result(api.apply(Request("/users/bar/10")))
}

class FinchTwFutureBench extends Bench {
  import futureEndpoint._

  @Benchmark
  def requestBench: Response =
    Await.result(api.apply(Request("/users/bar/10")))
}

class FinchMonixTaskBench extends Bench {
  import monix.execution.Scheduler.Implicits.global

  object monixEndpoint extends EffectEndpoint[Task]
  import monixEndpoint._

  @Benchmark
  def requestBench: Response =
    Await.result(api.apply(Request("/users/bar/10")))
}
