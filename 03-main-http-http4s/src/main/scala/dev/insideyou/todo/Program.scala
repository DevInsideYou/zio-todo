package dev.insideyou
package todo

import zio.*

object Program:
  lazy val make: Z[Unit] =
    makeDb
      .flatMap(makeControllers)
      .flatMap(makeServer)
      .flatMap(_.serve)

  private lazy val makeDb: UIO[Ref[Vector[crud.Todo[Int]]]] =
    Ref.make(Vector.empty)

  private def makeControllers(db: Ref[Vector[crud.Todo[Int]]]): Z[List[Controller]] =
    List(
      crud.make(Pattern, db),
      crud.insert.make(Pattern, db),
    ).sequence

  private lazy val Pattern: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy HH:mm")

  private def makeServer(controllers: List[Controller]): UIO[Server[ZEnv, Throwable]] =
    Server.make(HttpApp.make(controllers))
