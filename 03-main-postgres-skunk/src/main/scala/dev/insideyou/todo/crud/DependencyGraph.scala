package dev.insideyou
package todo
package crud

import cats.*

object DependencyGraph:
  def make[F[_]: effect.Sync](
      pattern: DateTimeFormatter,
      console: Console[F],
      random: Random[F],
      resource: effect.Resource[F, skunk.Session[F]],
    ): F[Controller[F]] =
    PostgresEntityGateway.make(resource).map { gateway =>
      Controller.make(
        pattern = pattern,
        boundary = BoundaryOld.make(gateway),
        console = FancyConsole.make(console),
        random = random,
      )
    }
