package dev.insideyou
package todo
package crud

import zio.*

trait Statement[TodoId, -R, +E]:
  def insertOne(data: Todo.Data): ZIO[R, E, Todo.Existing[TodoId]]
  def updateOne(todo: Todo.Existing[TodoId]): ZIO[R, E, Todo.Existing[TodoId]]
  def selectAll: ZIO[R, E, Vector[Todo.Existing[TodoId]]]
  def deleteMany(todos: Vector[Todo.Existing[TodoId]]): ZIO[R, E, Unit]
  def deleteAll: ZIO[R, E, Unit]

object Statement:
  def make(
      state: Ref[Vector[Todo.Existing[Int]]]
    ): Statement[Int, Any, Throwable] =
    new:
      override lazy val selectAll: Task[Vector[Todo.Existing[Int]]] =
        state.get

      private lazy val nextId: Task[Int] =
        selectAll.map(_.size)

      override def insertOne(data: Todo.Data): Task[Todo.Existing[Int]] =
        nextId
          .map(new Todo.Existing(_, data))
          .flatMap(created => state.modify(s => created -> (s :+ created)))

      override def updateOne(todo: Todo.Existing[Int]): Task[Todo.Existing[Int]] =
        state.get.flatMap { s =>
          if s.exists(_.id === todo.id) then
            state.modify(s => todo -> (s.filterNot(_.id === todo.id) :+ todo))
          else
            ZIO.fail(
              RuntimeException(s"Failed to update todo: ${todo.id} because it didn't exist.")
            )
        }

      override def deleteMany(todos: Vector[Todo.Existing[Int]]): Task[Unit] =
        state.update(_.filterNot(todo => todos.map(_.id).contains(todo.id)))

      override lazy val deleteAll: Task[Unit] =
        state.set(Vector.empty)
