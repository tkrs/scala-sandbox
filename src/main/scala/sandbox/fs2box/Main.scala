package sandbox.fs2box

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

object A {
  val line = new ByteArrayInputStream(s"""6
       |red
       |red
       |blue
       |yellow
       |yellow
       |red
       |5
       |red
       |red
       |yellow
       |green
       |blue
       |""".stripMargin.getBytes(StandardCharsets.UTF_8))
}

import A._

object Main extends App {
  val s = new java.util.Scanner(line)
  def f = { val n = s.nextInt; Iterator.continually(s.next).take(n) }
  val a = f.foldLeft(Map.empty[String, Int].withDefaultValue(0))((l, r) => l.updated(r, 1 + l(r)))
  val b = f.foldLeft(a)((l, r) => l.updated(r, l(r) - 1))
  println(Math.max(0, b.values.max))
}
