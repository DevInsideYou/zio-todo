package dev.insideyou
package todo

import scala.concurrent.*

import cats.effect.*

object ProgramOld:
  def make[F[_]: Async: std.Console: natchez.Trace](executionContext: ExecutionContext): F[Unit] =
    SessionPoolOld.make.use { resource =>
      for
        controller <- crud.DependencyGraphOld.make(Pattern, resource)
        server <- ServerOld.make(executionContext) {
          HttpAppOld.make(
            controller
          )
        }
        _ <- server.serve
      yield ()
    }

  private lazy val Pattern =
    DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy HH:mm")