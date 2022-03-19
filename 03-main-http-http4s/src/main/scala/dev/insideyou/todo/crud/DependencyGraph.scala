package dev.insideyou
package todo
package crud

import zio.*

object DependencyGraph:
  def make(pattern: DateTimeFormatter): UIO[Controller] =
    Ref.make(Vector.empty[Todo[Int]]).flatMap { state =>
      Controller.make(
        pattern = pattern,
        boundary = Boundary.make(InMemoryGate.make(state)),
      )
    }
