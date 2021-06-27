package dev.insideyou
package todo
package crud

import zio.*

trait EntityGateway[TodoId, -R, +E]:
  def createMany(todos: Vector[Todo.Data]): ZIO[R, E, Vector[Todo.Existing[TodoId]]]
  def updateMany(todos: Vector[Todo.Existing[TodoId]]): ZIO[R, E, Vector[Todo.Existing[TodoId]]]

  def readManyById(ids: Vector[TodoId]): ZIO[R, E, Vector[Todo.Existing[TodoId]]]
  def readManyByPartialDescription(
      partialDescription: String
    ): ZIO[R, E, Vector[Todo.Existing[TodoId]]]
  def readAll: ZIO[R, E, Vector[Todo.Existing[TodoId]]]

  def deleteMany(todos: Vector[Todo.Existing[TodoId]]): ZIO[R, E, Unit]
  def deleteAll: ZIO[R, E, Unit]
