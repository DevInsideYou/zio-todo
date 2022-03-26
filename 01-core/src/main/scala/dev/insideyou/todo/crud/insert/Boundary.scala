package dev.insideyou
package todo
package crud
package insert

import zio.*

trait Boundary[-R, +E, TodoId]:
  def createOne(todo: Todo): ZIO[R, E, crud.Todo[TodoId]]
  def createMany(todos: Vector[Todo]): ZIO[R, E, Vector[crud.Todo[TodoId]]]

object Boundary:
  def make[R, TodoId](
      gate: Gate[R, Throwable, TodoId]
    ): Boundary[R, Throwable, TodoId] =
    new:
      override def createOne(todo: Todo): RIO[R, crud.Todo[TodoId]] =
        createMany(Vector(todo)).map(_.head)

      override def createMany(todos: Vector[Todo]): RIO[R, Vector[crud.Todo[TodoId]]] =
        gate.createMany(
          todos.map { todo =>
            todo.withUpdatedDescription(todo.description.trim.nn)
          }
        )
