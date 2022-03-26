package dev.insideyou
package todo

import zio.*

object Program:
  lazy val make: UIO[Unit] =
    for
      db <- Ref.make(Vector.empty[crud.Todo[Int]])
      (console, random) <- Console.make.zipPar(Random.make)
      controller <- crud.make(Pattern, console, random, db)
      _ <- controller.program
    yield ()

  private lazy val Pattern: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy HH:mm")
