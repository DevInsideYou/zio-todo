package dev.insideyou
package todo
package crud
package insert

import zio.*

def boundary(db: Ref[Vector[crud.Todo[Int]]]): Boundary[Any, Throwable, Int] =
  Boundary.make(InMemoryGate.make(db))