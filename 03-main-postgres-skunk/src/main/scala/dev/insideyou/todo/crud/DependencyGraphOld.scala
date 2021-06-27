package dev.insideyou
package todo
package crud

import cats.*

object DependencyGraphOld:
  def make[F[_]: effect.Sync](
      pattern: DateTimeFormatter,
      console: ConsoleOld[F],
      random: RandomOld[F],
      resource: effect.Resource[F, skunk.Session[F]],
    ): F[ControllerOld[F]] =
    PostgresEntityGatewayOld.make(resource).map { gateway =>
      ControllerOld.make(
        pattern = pattern,
        boundary = BoundaryOld.make(gateway),
        console = FancyConsoleOld.make(console),
        random = random,
      )
    }