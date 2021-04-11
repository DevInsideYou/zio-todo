package dev.insideyou
package todo
package crud

import cats.*
import cats.effect.Ref

trait Statement[F[_], TodoId]:
  def insertOne(data: Todo.Data): F[Todo.Existing[TodoId]]
  def updateOne(todo: Todo.Existing[TodoId]): F[Todo.Existing[TodoId]]
  def selectAll: F[Vector[Todo.Existing[TodoId]]]
  def deleteMany(todos: Vector[Todo.Existing[TodoId]]): F[Unit]
  def deleteAll: F[Unit]

object Statement:
  def make[F[_]](
      state: Ref[F, Vector[Todo.Existing[Int]]]
    )(using
      ME: MonadError[F, Throwable]
    ): Statement[F, Int] =
    new:
      override lazy val selectAll: F[Vector[Todo.Existing[Int]]] =
        state.get

      private lazy val nextId: F[Int] =
        selectAll.map(_.size)

      override def insertOne(data: Todo.Data): F[Todo.Existing[Int]] =
        nextId
          .map(new Todo.Existing(_, data))
          .flatMap { created =>
            state.modify(s => (s :+ created) -> created)
          }

      override def updateOne(todo: Todo.Existing[Int]): F[Todo.Existing[Int]] =
        state.get.flatMap { s =>
          if s.exists(_.id === todo.id) then
            state.modify { s =>
              (s.filterNot(_.id === todo.id) :+ todo) -> todo
            }
          else
            ME.raiseError(
              RuntimeException(s"Failed to update todo: ${todo.id} because it didn't exist.")
            )
        }

      override def deleteMany(todos: Vector[Todo.Existing[Int]]): F[Unit] =
        state.update(_.filterNot(todo => todos.map(_.id).contains(todo.id)))

      override lazy val deleteAll: F[Unit] =
        state.set(Vector.empty)
