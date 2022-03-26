package dev.insideyou
package todo
package crud
package insert

import zio.*

trait Boundary[-R, +E, TodoId]:
  def createOne(todo: Todo): ZIO[R, E, crud.Todo[TodoId]]
  def createMany(todos: Vector[Todo]): ZIO[R, E, Vector[crud.Todo[TodoId]]]
