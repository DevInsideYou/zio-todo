package dev.insideyou
package todo
package crud

import zio.*

def make(pattern: DateTimeFormatter, db: Ref[Vector[Todo[Int]]]): UIO[Controller] =
  Controller.make(
    pattern = pattern,
    boundary = BoundaryImpl.make(InMemoryGate.make(db)),
  )
