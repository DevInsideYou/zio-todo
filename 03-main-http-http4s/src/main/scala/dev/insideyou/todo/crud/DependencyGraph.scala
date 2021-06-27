package dev.insideyou
package todo
package crud

import cats.*
import cats.effect.Ref

object DependencyGraph:
  def make[F[_]: effect.Async](pattern: DateTimeFormatter): F[ControllerOld[F]] =
    Ref.of[F, Vector[Todo.Existing[Int]]](Vector.empty).flatMap { state =>
      ControllerOld.make(
        pattern = pattern,
        boundary = BoundaryOld.make(
          gateway = InMemoryEntityGatewayOld.make(state)
        ),
      )
    }
