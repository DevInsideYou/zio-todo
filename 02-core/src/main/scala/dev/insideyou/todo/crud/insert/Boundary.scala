package dev.insideyou
package todo
package crud
package insert

import zio.*

object BoundaryImpl:
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
