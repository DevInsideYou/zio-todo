package dev.insideyou
package todo
package crud

import cats.*
import cats.effect.Ref

object DependencyGraph:
  def make[F[_]: effect.Sync](
      pattern: DateTimeFormatter,
      console: ConsoleOld[F],
      random: RandomOld[F],
    ): F[ControllerOld[F]] =
    Ref.of[F, Vector[Todo.Existing[Int]]](Vector.empty).map { state =>
      ControllerOld.make(
        pattern = pattern,
        boundary = BoundaryOld.make(
          gateway = InMemoryEntityGatewayOld.make(state)
        ),
        console = FancyConsoleOld.make(console),
        random = random,
      )
    }
