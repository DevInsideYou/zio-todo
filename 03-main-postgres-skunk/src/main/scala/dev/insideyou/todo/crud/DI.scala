package dev.insideyou
package todo
package crud

import zio.*

def make(
    pattern: DateTimeFormatter,
    console: Console[Any, Nothing],
    random: Random[Any, Nothing],
    resource: RManaged[ZEnv, skunk.Session[Z]],
  ): UIO[Controller[ZEnv, Nothing]] =
  PostgresGate.make(resource).zipPar(insert.boundary(resource)).map { (gate, insertBoundary) =>
    Controller.make(
      pattern = pattern,
      boundary = Boundary.make(gate),
      insertBoundary = insertBoundary,
      console = FancyConsole.make(console),
      random = random,
    )
  }
