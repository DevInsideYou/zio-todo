package dev.insideyou
package todo

import org.http4s.*
import org.http4s.implicits.*
import org.http4s.server.Router
import org.http4s.server.middleware.Logger
import zio.*

object HttpApp:
  def make(controllers: List[Controller]): HttpApp[Z] =
    controllers
      .map(_.routes)
      .reduceLeft(_ <+> _)
      .pipe(routes => Router("api" -> routes))
      .orNotFound
      .pipe(Logger.httpApp(logHeaders = true, logBody = true))
