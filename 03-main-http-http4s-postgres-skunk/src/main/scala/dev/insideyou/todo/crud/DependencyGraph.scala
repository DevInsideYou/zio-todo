package dev.insideyou
package todo
package crud

import cats.*

object DependencyGraph:
  def make[F[_]: effect.Async](
      pattern: DateTimeFormatter,
      resource: effect.Resource[F, skunk.Session[F]],
    ): F[Controller[F]] =
    PostgresEntityGateway.make(resource).flatMap { gateway =>
      Controller.make(
        pattern = pattern,
        boundary = Boundary.make(gateway),
      )
    }
