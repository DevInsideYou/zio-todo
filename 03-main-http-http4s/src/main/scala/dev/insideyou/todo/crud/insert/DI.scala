package dev.insideyou
package todo
package crud
package insert

import zio.*

def make(pattern: DateTimeFormatter, db: Ref[Vector[crud.Todo[Int]]]): UIO[Controller] =
  Controller.make(
    pattern = pattern,
    boundary = Boundary.make(InMemoryGate.make(db)),
  )
