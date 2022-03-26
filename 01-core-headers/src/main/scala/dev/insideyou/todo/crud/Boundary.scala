package dev.insideyou
package todo
package crud

import zio.*

trait Boundary[-R, +E, TodoId]:
  def readOneById(id: TodoId): ZIO[R, E, Option[Todo[TodoId]]]
  def readManyById(ids: Vector[TodoId]): ZIO[R, E, Vector[Todo[TodoId]]]
  def readManyByDescription(description: String): ZIO[R, E, Vector[Todo[TodoId]]]
  def readAll: ZIO[R, E, Vector[Todo[TodoId]]]

  def updateOne(todo: Todo[TodoId]): ZIO[R, E, Todo[TodoId]]
  def updateMany(todos: Vector[Todo[TodoId]]): ZIO[R, E, Vector[Todo[TodoId]]]

  def deleteOne(todo: Todo[TodoId]): ZIO[R, E, Unit]
  def deleteMany(todos: Vector[Todo[TodoId]]): ZIO[R, E, Unit]
  def deleteAll: ZIO[R, E, Unit]
