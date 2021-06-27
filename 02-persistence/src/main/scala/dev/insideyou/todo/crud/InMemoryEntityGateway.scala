package dev.insideyou
package todo
package crud

import cats.*
import cats.effect.Ref

object InMemoryEntityGateway:
  def make[F[_]](
      state: Ref[F, Vector[Todo.Existing[Int]]]
    )(using
      MonadError[F, Throwable]
    ): EntityGatewayOld[F, Int] =
    new:
      private lazy val statement: Statement[F, Int] =
        Statement.make(state)

      override def createMany(todos: Vector[Todo.Data]): F[Vector[Todo.Existing[Int]]] =
        todos.traverse(statement.insertOne)

      override def updateMany(todos: Vector[Todo.Existing[Int]]): F[Vector[Todo.Existing[Int]]] =
        todos.traverse(statement.updateOne)

      override def readManyById(ids: Vector[Int]): F[Vector[Todo.Existing[Int]]] =
        statement
          .selectAll
          .map(_.filter(todo => ids.contains(todo.id)))

      override def readManyByPartialDescription(
          partialDescription: String
        ): F[Vector[Todo.Existing[Int]]] =
        statement
          .selectAll
          .map(_.filter(_.description.toLowerCase.contains(partialDescription.toLowerCase)))

      override lazy val readAll: F[Vector[Todo.Existing[Int]]] =
        statement.selectAll

      override def deleteMany(todos: Vector[Todo.Existing[Int]]): F[Unit] =
        statement.deleteMany(todos)

      override lazy val deleteAll: F[Unit] =
        statement.deleteAll
