package sandbox

import java.time.Instant

object Log {
  def println(x: Any): Unit =
    scala.Predef.println(s"${Instant.now()} [${Thread.currentThread().getName}] $x")
}
