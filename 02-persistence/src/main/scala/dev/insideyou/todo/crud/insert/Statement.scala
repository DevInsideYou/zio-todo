package dev.insideyou
package todo
package crud
package insert

import zio.*

trait Statement[-R, +E, TodoId]:
  def insertOne(todo: Todo): ZIO[R, E, crud.Todo[TodoId]]
  def selectAll: ZIO[R, E, Vector[crud.Todo[TodoId]]]

object Statement:
  def make(state: Ref[Vector[crud.Todo[Int]]]): Statement[Any, Throwable, Int] =
    new:
      override lazy val selectAll: Task[Vector[crud.Todo[Int]]] =
        state.get

      private lazy val nextId: Task[Int] =
        selectAll.map(_.size)

      override def insertOne(todo: Todo): Task[crud.Todo[Int]] =
        nextId
          .map(crud.Todo(_, todo.description, todo.deadline))
          .flatMap(created => state.modify(s => created -> (s :+ created)))
