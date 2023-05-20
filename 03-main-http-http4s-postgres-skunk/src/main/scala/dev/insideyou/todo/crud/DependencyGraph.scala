package dev.insideyou
package todo
package crud

import zio.*

object DependencyGraph:
  def make(
    pattern: DateTimeFormatter,
    session: skunk.Session[Z],
  ): UIO[Controller] =
    PostgresGate
      .make(session)
      .flatMap: gate =>
        Controller.make(
          pattern = pattern,
          boundary = Boundary.make(gate),
        )
