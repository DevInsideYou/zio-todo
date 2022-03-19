package dev.insideyou
package todo
package crud

import zio.*

trait Statement[-R, +E, TodoId]:
  def insertOne(todo: insert.Todo): ZIO[R, E, Todo[TodoId]]
  def updateOne(todo: Todo[TodoId]): ZIO[R, E, Todo[TodoId]]
  def selectAll: ZIO[R, E, Vector[Todo[TodoId]]]
  def deleteMany(todos: Vector[Todo[TodoId]]): ZIO[R, E, Unit]
  def deleteAll: ZIO[R, E, Unit]

object Statement:
  def make(state: Ref[Vector[Todo[Int]]]): Statement[Any, Throwable, Int] =
    new:
      override lazy val selectAll: Task[Vector[Todo[Int]]] =
        state.get

      private lazy val nextId: Task[Int] =
        selectAll.map(_.size)

      override def insertOne(todo: insert.Todo): Task[Todo[Int]] =
        nextId
          .map(new Todo(_, todo.description, todo.deadline))
          .flatMap(created => state.modify(s => created -> (s :+ created)))

      override def updateOne(todo: Todo[Int]): Task[Todo[Int]] =
        state.get.flatMap { s =>
          if s.exists(_.id === todo.id) then
            state.modify(s => todo -> (s.filterNot(_.id === todo.id) :+ todo))
          else
            ZIO.fail(
              RuntimeException(s"Failed to update todo: ${todo.id} because it didn't exist.")
            )
        }

      override def deleteMany(todos: Vector[Todo[Int]]): Task[Unit] =
        state.update(_.filterNot(todo => todos.map(_.id).contains(todo.id)))

      override lazy val deleteAll: Task[Unit] =
        state.set(Vector.empty)
