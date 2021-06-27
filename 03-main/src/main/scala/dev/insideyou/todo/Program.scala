package dev.insideyou
package todo

import zio.*

object Program:
  lazy val make: UIO[Unit] =
    for
      console <- Console.make
      random <- Random.make
      controller <- crud.DependencyGraph.make(Pattern, console, random)
      _ <- controller.program
    yield ()

  private lazy val Pattern =
    DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy HH:mm")
