package dev.insideyou
package todo
package crud

import zio.*

object DependencyGraph:
  def make(
      pattern: DateTimeFormatter,
      resource: RManaged[ZEnv, skunk.Session[Z]],
    ): UIO[Controller] =
    PostgresEntityGateway.make(resource).flatMap { gateway =>
      Controller.make(
        pattern = pattern,
        boundary = Boundary.make(gateway),
      )
    }
