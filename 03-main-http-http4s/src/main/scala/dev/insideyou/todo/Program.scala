package dev.insideyou
package todo

import scala.concurrent.*

import cats.*

object Program:
  def make[F[_]: effect.Async](executionContext: ExecutionContext): F[Unit] =
    for
      controller <- crud.DependencyGraph.make(Pattern)
      server <- ServerOld.make(executionContext) {
        HttpAppOld.make(
          controller
        )
      }
      _ <- server.serve
    yield ()

  private lazy val Pattern =
    DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy HH:mm")
