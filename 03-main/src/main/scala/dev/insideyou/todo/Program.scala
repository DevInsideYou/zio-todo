package dev.insideyou
package todo

import cats.*

object Program:
  def make[F[_]: effect.Sync]: F[Unit] =
    for
      console <- Console.make
      random <- Random.make
      controller <- crud.DependencyGraph.make(Pattern, console, random)
      _ <- controller.program
    yield ()

  private lazy val Pattern =
    DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy HH:mm")
