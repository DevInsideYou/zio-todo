package dev.insideyou
package todo

import cats.effect.*

object Program:
  lazy val make: Z[Unit] =
    SessionPool.make.use { resource =>
      for
        (console, random) <- Console.make.zipPar(Random.make)
        controller <- crud.make(Pattern, console, random, resource)
        _ <- controller.program
      yield ()
    }

  private lazy val Pattern: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy HH:mm")
