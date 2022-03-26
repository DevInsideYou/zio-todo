package dev.insideyou
package todo
package crud
package insert

import zio.*

object InMemoryGate:
  def make(state: Ref[Vector[crud.Todo[Int]]]): Gate[Any, Throwable, Int] =
    new:
      private lazy val statement: Statement[Any, Throwable, Int] =
        Statement.make(state)

      override def createMany(todos: Vector[Todo]): Task[Vector[crud.Todo[Int]]] =
        ZIO.foreach(todos)(statement.insertOne)
