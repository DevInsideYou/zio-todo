package dev.insideyou
package todo
package crud

import zio.*

trait Boundary[-R, +E, TodoId]:
  def createOne(todo: Todo.Data): ZIO[R, E, Todo.Existing[TodoId]]
  def createMany(todos: Vector[Todo.Data]): ZIO[R, E, Vector[Todo.Existing[TodoId]]]

  def readOneById(id: TodoId): ZIO[R, E, Option[Todo.Existing[TodoId]]]
  def readManyById(ids: Vector[TodoId]): ZIO[R, E, Vector[Todo.Existing[TodoId]]]
  def readManyByDescription(description: String): ZIO[R, E, Vector[Todo.Existing[TodoId]]]
  def readAll: ZIO[R, E, Vector[Todo.Existing[TodoId]]]

  def updateOne(todo: Todo.Existing[TodoId]): ZIO[R, E, Todo.Existing[TodoId]]
  def updateMany(todos: Vector[Todo.Existing[TodoId]]): ZIO[R, E, Vector[Todo.Existing[TodoId]]]

  def deleteOne(todo: Todo.Existing[TodoId]): ZIO[R, E, Unit]
  def deleteMany(todos: Vector[Todo.Existing[TodoId]]): ZIO[R, E, Unit]
  def deleteAll: ZIO[R, E, Unit]

object Boundary:
  def make[R, TodoId](gate: Gate[R, Throwable, TodoId]): Boundary[R, Throwable, TodoId] =
    new:
      override def createOne(todo: Todo.Data): RIO[R, Todo.Existing[TodoId]] =
        createMany(Vector(todo)).map(_.head)

      override def createMany(todos: Vector[Todo.Data]): RIO[R, Vector[Todo.Existing[TodoId]]] =
        gate.createMany:
          todos.map: todo =>
            todo.withUpdatedDescription(todo.description.trim.nn)

      override def updateMany(
        todos: Vector[Todo.Existing[TodoId]]
      ): RIO[R, Vector[Todo.Existing[TodoId]]] =
        gate.updateMany:
          todos.map: todo =>
            todo.withUpdatedDescription(todo.description.trim.nn)

      override def readOneById(id: TodoId): RIO[R, Option[Todo.Existing[TodoId]]] =
        readManyById(Vector(id)).map(_.headOption)

      override def readManyById(ids: Vector[TodoId]): RIO[R, Vector[Todo.Existing[TodoId]]] =
        gate.readManyById(ids)

      override def readManyByDescription(
        description: String
      ): RIO[R, Vector[Todo.Existing[TodoId]]] =
        if description.isEmpty then ZIO.succeed(Vector.empty)
        else gate.readManyByDescription(description.trim.nn)

      override lazy val readAll: RIO[R, Vector[Todo.Existing[TodoId]]] =
        gate.readAll

      override def updateOne(todo: Todo.Existing[TodoId]): RIO[R, Todo.Existing[TodoId]] =
        updateMany(Vector(todo)).map(_.head)

      override def deleteOne(todo: Todo.Existing[TodoId]): RIO[R, Unit] =
        deleteMany(Vector(todo))

      override def deleteMany(todos: Vector[Todo.Existing[TodoId]]): RIO[R, Unit] =
        gate.deleteMany(todos)

      override lazy val deleteAll: RIO[R, Unit] =
        gate.deleteAll
