package dev.insideyou
package todo

object Program:
  lazy val make: Z[Unit] =
    SessionPool
      .make
      .use: session =>
        for
          console -> random <- Console.make.zipPar(Random.make)
          controller <- crud.DependencyGraph.make(Pattern, console, random, session)
          _ <- controller.program
        yield ()

  private lazy val Pattern =
    DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy HH:mm")
