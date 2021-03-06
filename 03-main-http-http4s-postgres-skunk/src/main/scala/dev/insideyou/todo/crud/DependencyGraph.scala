package dev.insideyou
package todo
package crud

import zio.*

object DependencyGraph:
  def make(
      pattern: DateTimeFormatter,
      resource: RManaged[ZEnv, skunk.Session[Z]],
    ): UIO[Controller] =
    PostgresGate.make(resource).flatMap { gate =>
      Controller.make(
        pattern = pattern,
        boundary = Boundary.make(gate),
      )
    }
