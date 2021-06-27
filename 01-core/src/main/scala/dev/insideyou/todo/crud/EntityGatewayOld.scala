package dev.insideyou
package todo
package crud

trait EntityGatewayOld[F[_], TodoId]:
  def createMany(todos: Vector[Todo.Data]): F[Vector[Todo.Existing[TodoId]]]
  def updateMany(todos: Vector[Todo.Existing[TodoId]]): F[Vector[Todo.Existing[TodoId]]]

  def readManyById(ids: Vector[TodoId]): F[Vector[Todo.Existing[TodoId]]]
  def readManyByPartialDescription(partialDescription: String): F[Vector[Todo.Existing[TodoId]]]
  def readAll: F[Vector[Todo.Existing[TodoId]]]

  def deleteMany(todos: Vector[Todo.Existing[TodoId]]): F[Unit]
  def deleteAll: F[Unit]
