package dev.insideyou
package todo
package crud

import zio.*

object BoundaryImpl:
  def make[R, TodoId](
      gate: Gate[R, Throwable, TodoId]
    ): Boundary[R, Throwable, TodoId] =
    new:
      override def updateMany(todos: Vector[Todo[TodoId]]): RIO[R, Vector[Todo[TodoId]]] =
        gate.updateMany:
          todos.map: todo =>
            todo.withUpdatedDescription(todo.description.trim.nn)

      override def readOneById(id: TodoId): RIO[R, Option[Todo[TodoId]]] =
        readManyById(Vector(id)).map(_.headOption)

      override def readManyById(ids: Vector[TodoId]): RIO[R, Vector[Todo[TodoId]]] =
        gate.readManyById(ids)

      override def readManyByDescription(description: String): RIO[R, Vector[Todo[TodoId]]] =
        if description.isEmpty then ZIO.succeed(Vector.empty)
        else gate.readManyByDescription(description.trim.nn)

      override lazy val readAll: RIO[R, Vector[Todo[TodoId]]] =
        gate.readAll

      override def updateOne(todo: Todo[TodoId]): RIO[R, Todo[TodoId]] =
        updateMany(Vector(todo)).map(_.head)

      override def deleteOne(todo: Todo[TodoId]): RIO[R, Unit] =
        deleteMany(Vector(todo))

      override def deleteMany(todos: Vector[Todo[TodoId]]): RIO[R, Unit] =
        gate.deleteMany(todos)

      override lazy val deleteAll: RIO[R, Unit] =
        gate.deleteAll
