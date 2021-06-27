package dev.insideyou
package todo
package crud

import zio.*

object DependencyGraph:
  def make(
      pattern: DateTimeFormatter,
      console: Console[Any, Nothing],
      random: Random[Any, Nothing],
      resource: RManaged[ZEnv, skunk.Session[Z]],
    ): UIO[Controller[ZEnv, Nothing]] =
    PostgresEntityGateway.make(resource).map { gateway =>
      Controller.make(
        pattern = pattern,
        boundary = Boundary.make(gateway),
        console = FancyConsole.make(console),
        random = random,
      )
    }
