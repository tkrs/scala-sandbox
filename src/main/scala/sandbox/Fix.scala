package sandbox

final case class Fix[F[_]](int: Int, str: String, nested: F[Fix[F]])
