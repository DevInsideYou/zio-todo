package dev.insideyou
package todo
package crud

import cats.*

object DependencyGraphOld:
  def make[F[_]: effect.Async](
      pattern: DateTimeFormatter,
      resource: effect.Resource[F, skunk.Session[F]],
    ): F[ControllerOld[F]] =
    PostgresEntityGatewayOld.make(resource).flatMap { gateway =>
      ControllerOld.make(
        pattern = pattern,
        boundary = BoundaryOld.make(gateway),
      )
    }
