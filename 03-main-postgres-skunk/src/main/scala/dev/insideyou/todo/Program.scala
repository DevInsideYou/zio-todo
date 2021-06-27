package dev.insideyou
package todo

import cats.effect.*

object Program:
  lazy val make: Z[Unit] =
    SessionPool.make.use { resource =>
      for
        console <- Console.make
        random <- Random.make
        controller <-
          crud.DependencyGraph.make(Pattern, console, random, resource)
        _ <- controller.program
      yield ()
    }

  private lazy val Pattern =
    DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy HH:mm")
