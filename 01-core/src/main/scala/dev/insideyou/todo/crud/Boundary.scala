package dev.insideyou
package todo
package crud

import cats.*

trait Boundary[F[_], TodoId]:
  def createOne(todo: Todo.Data): F[Todo.Existing[TodoId]]
  def createMany(todos: Vector[Todo.Data]): F[Vector[Todo.Existing[TodoId]]]

  def readOneById(id: TodoId): F[Option[Todo.Existing[TodoId]]]
  def readManyById(ids: Vector[TodoId]): F[Vector[Todo.Existing[TodoId]]]
  def readManyByPartialDescription(partialDescription: String): F[Vector[Todo.Existing[TodoId]]]
  def readAll: F[Vector[Todo.Existing[TodoId]]]

  def updateOne(todo: Todo.Existing[TodoId]): F[Todo.Existing[TodoId]]
  def updateMany(todos: Vector[Todo.Existing[TodoId]]): F[Vector[Todo.Existing[TodoId]]]

  def deleteOne(todo: Todo.Existing[TodoId]): F[Unit]
  def deleteMany(todos: Vector[Todo.Existing[TodoId]]): F[Unit]
  def deleteAll: F[Unit]

object Boundary:
  def make[F[_]: Applicative, TodoId](
      gateway: EntityGateway[F, TodoId]
    ): Boundary[F, TodoId] =
    new:
      override def createOne(todo: Todo.Data): F[Todo.Existing[TodoId]] =
        createMany(Vector(todo)).map(_.head)

      override def createMany(todos: Vector[Todo.Data]): F[Vector[Todo.Existing[TodoId]]] =
        gateway.createMany(
          todos.map { todo =>
            todo.withUpdatedDescription(todo.description.trim)
          }
        )

      override def updateMany(
          todos: Vector[Todo.Existing[TodoId]]
        ): F[Vector[Todo.Existing[TodoId]]] =
        gateway.updateMany(
          todos.map { todo =>
            todo.withUpdatedDescription(todo.description.trim)
          }
        )

      override def readOneById(id: TodoId): F[Option[Todo.Existing[TodoId]]] =
        readManyById(Vector(id)).map(_.headOption)

      override def readManyById(ids: Vector[TodoId]): F[Vector[Todo.Existing[TodoId]]] =
        gateway.readManyById(ids)

      override def readManyByPartialDescription(
          partialDescription: String
        ): F[Vector[Todo.Existing[TodoId]]] =
        if partialDescription.isEmpty then Vector.empty.pure[F]
        else gateway.readManyByPartialDescription(partialDescription.trim)

      override lazy val readAll: F[Vector[Todo.Existing[TodoId]]] =
        gateway.readAll

      override def updateOne(todo: Todo.Existing[TodoId]): F[Todo.Existing[TodoId]] =
        updateMany(Vector(todo)).map(_.head)

      override def deleteOne(todo: Todo.Existing[TodoId]): F[Unit] =
        deleteMany(Vector(todo))

      override def deleteMany(todos: Vector[Todo.Existing[TodoId]]): F[Unit] =
        gateway.deleteMany(todos)

      override lazy val deleteAll: F[Unit] =
        gateway.deleteAll
