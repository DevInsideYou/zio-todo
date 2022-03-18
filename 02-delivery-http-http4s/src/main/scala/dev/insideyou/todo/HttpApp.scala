package dev.insideyou
package todo

import org.http4s.*
import org.http4s.implicits.*
import org.http4s.server.Router
import org.http4s.server.middleware.Logger
import zio.*

object HttpApp:
  def make(first: Controller, remaining: Controller*): HttpApp[Z] =
    (first +: remaining)
      .map(_.routes)
      .reduceLeft(_ <+> _)
      .pipe(routes => Router("api" -> routes))
      .orNotFound
      .pipe(Logger.httpApp(logHeaders = true, logBody = true))
