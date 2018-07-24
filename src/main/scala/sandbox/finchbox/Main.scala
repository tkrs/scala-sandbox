package sandbox.finchbox

import com.twitter.finagle.http.{Cookie, Request}

object Main extends App {
  val req: Request = Request("/")
  req.addCookie(new Cookie("a", "b"))
}
