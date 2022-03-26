package dev.insideyou
package todo
package crud
package insert

import zio.*

trait Gate[-R, +E, TodoId]:
  def createMany(todos: Vector[Todo]): ZIO[R, E, Vector[crud.Todo[TodoId]]]
