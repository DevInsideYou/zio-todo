package dev.insideyou
package todo
package crud

import cats.*
import cats.effect.Ref

object DependencyGraph:
  def make[F[_]: effect.Sync](
      pattern: DateTimeFormatter,
      console: Console[F],
      random: Random[F],
    ): F[Controller[F]] =
    Ref.of[F, Vector[Todo.Existing[Int]]](Vector.empty).map { state =>
      Controller.make(
        pattern = pattern,
        boundary = BoundaryOld.make(
          gateway = InMemoryEntityGateway.make(state)
        ),
        console = FancyConsole.make(console),
        random = random,
      )
    }
