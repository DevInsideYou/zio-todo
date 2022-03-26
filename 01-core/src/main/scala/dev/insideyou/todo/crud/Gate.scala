package dev.insideyou
package todo
package crud

import zio.*

trait Gate[-R, +E, TodoId]:
  def updateMany(todos: Vector[Todo[TodoId]]): ZIO[R, E, Vector[Todo[TodoId]]]

  def readManyById(ids: Vector[TodoId]): ZIO[R, E, Vector[Todo[TodoId]]]
  def readManyByDescription(description: String): ZIO[R, E, Vector[Todo[TodoId]]]
  def readAll: ZIO[R, E, Vector[Todo[TodoId]]]

  def deleteMany(todos: Vector[Todo[TodoId]]): ZIO[R, E, Unit]
  def deleteAll: ZIO[R, E, Unit]
