package dev.insideyou
package todo
package crud

import zio.*

object DependencyGraph:
  def make(
      pattern: DateTimeFormatter,
      console: Console[Any, Nothing],
      random: Random[Any, Nothing],
    ): UIO[Controller[Any, Nothing]] =
    Ref.make(Vector.empty[Todo.Existing[Int]]).map { state =>
      Controller.make(
        pattern = pattern,
        boundary = Boundary.make(
          gateway = InMemoryEntityGateway.make(state)
        ),
        console = FancyConsole.make(console),
        random = random,
      )
    }
