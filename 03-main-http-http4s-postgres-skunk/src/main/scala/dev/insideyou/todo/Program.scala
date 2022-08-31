package dev.insideyou
package todo

import zio.*
import skunk.Session

object Program:
  lazy val make: Z[Unit] =
    SessionPool.make.use: resource =>
      makeControllers(resource)
        .flatMap(makeServer)
        .flatMap(_.serve)

  private def makeControllers(resource: RManaged[ZEnv, Session[Z]]): Z[List[Controller]] =
    List(
      crud.make(Pattern, resource),
      crud.insert.make(Pattern, resource),
    ).sequence

  private lazy val Pattern: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy HH:mm")

  private def makeServer(controllers: List[Controller]): UIO[Server[ZEnv, Throwable]] =
    Server.make(HttpApp.make(controllers))
