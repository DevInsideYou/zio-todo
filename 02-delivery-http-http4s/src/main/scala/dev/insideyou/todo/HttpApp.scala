package dev.insideyou
package todo

import cats.*
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.server.Router
import org.http4s.server.middleware.Logger

object HttpApp:
  def make[F[_]: effect.Async](
      first: Controller[F],
      remaining: Controller[F]*
    ): HttpApp[F] =
    (first +: remaining)
      .map(_.routes)
      .reduceLeft(_ <+> _)
      .pipe(routes => Router("api" -> routes))
      .orNotFound
      .pipe(Logger.httpApp(logHeaders = true, logBody = true))
