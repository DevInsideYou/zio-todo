package dev.insideyou
package todo

import cats.effect.*

object ProgramOld:
  def make[F[_]: Async: std.Console: natchez.Trace]: F[Unit] =
    SessionPoolOld.make.use { resource =>
      for
        console <- ConsoleOld.make
        random <- RandomOld.make
        controller <-
          crud.DependencyGraphOld.make(Pattern, console, random, resource)
        _ <- controller.program
      yield ()
    }

  private lazy val Pattern =
    DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy HH:mm")
