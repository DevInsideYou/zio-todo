package dev.insideyou
package todo
package crud

import zio.*

def make(
    pattern: DateTimeFormatter,
    console: Console[Any, Nothing],
    random: Random[Any, Nothing],
    db: Ref[Vector[crud.Todo[Int]]],
  ): UIO[Controller[Any, Nothing]] =
  ZIO.succeed:
    Controller.make(
      pattern = pattern,
      boundary = BoundaryImpl.make(InMemoryGate.make(db)),
      insertBoundary = insert.boundary(db),
      console = FancyConsole.make(console),
      random = random,
    )
