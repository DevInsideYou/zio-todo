package dev.insideyou
package todo
package crud

import zio.*

object InMemoryGate:
  def make(state: Ref[Vector[Todo[Int]]]): Gate[Any, Throwable, Int] =
    new:
      private lazy val statement: Statement[Any, Throwable, Int] =
        Statement.make(state)

      override def updateMany(todos: Vector[Todo[Int]]): Task[Vector[Todo[Int]]] =
        ZIO.foreach(todos)(statement.updateOne)

      override def readManyById(ids: Vector[Int]): Task[Vector[Todo[Int]]] =
        statement
          .selectAll
          .map(_.filter(todo => ids.contains(todo.id)))

      override def readManyByDescription(description: String): Task[Vector[Todo[Int]]] =
        statement
          .selectAll
          .map(_.filter(_.description.toLowerCase.contains(description.toLowerCase)))

      override lazy val readAll: Task[Vector[Todo[Int]]] =
        statement.selectAll

      override def deleteMany(todos: Vector[Todo[Int]]): Task[Unit] =
        statement.deleteMany(todos)

      override lazy val deleteAll: Task[Unit] =
        statement.deleteAll
