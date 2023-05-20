package dev.insideyou
package todo
package crud

import zio.*

object PostgresGate:
  def make(session: skunk.Session[Z]): UIO[Gate[ZEnv, Throwable, UUID]] =
    ZIO.succeed:
      new:
        override def createMany(todos: Vector[Todo.Data]): Z[Vector[Todo.Existing[UUID]]] =
          todos.traverse(insertOne)

        override def updateMany(
          todos: Vector[Todo.Existing[UUID]]
        ): Z[Vector[Todo.Existing[UUID]]] =
          todos.traverse(updateOne)

        private def insertOne(data: Todo.Data): Z[Todo.Existing[UUID]] =
          session
            .prepare(Statement.Insert.one)
            .flatMap: preparedQuery =>
              preparedQuery.unique(data)

        private def updateOne(todo: Todo.Existing[UUID]): Z[Todo.Existing[UUID]] =
          session
            .prepare(Statement.Update.one)
            .flatMap: preparedQuery =>
              preparedQuery.unique(todo)

        override def readManyById(ids: Vector[UUID]): Z[Vector[Todo.Existing[UUID]]] =
          session
            .prepare(Statement.Select.many(ids.size))
            .flatMap: preparedQuery =>
              preparedQuery
                .stream(ids.to(List), ChunkSizeInBytes)
                .compile
                .toVector

        override def readManyByDescription(description: String): Z[Vector[Todo.Existing[UUID]]] =
          session
            .prepare(Statement.Select.byDescription)
            .flatMap: preparedQuery =>
              preparedQuery
                .stream(description, ChunkSizeInBytes)
                .compile
                .toVector

        override lazy val readAll: Z[Vector[Todo.Existing[UUID]]] =
          session
            .execute(Statement.Select.all)
            .map(_.to(Vector))

        override def deleteMany(todos: Vector[Todo.Existing[UUID]]): Z[Unit] =
          session
            .prepare(Statement.Delete.many(todos.size))
            .flatMap: preparedCommand =>
              preparedCommand
                .execute(todos.to(List).map(_.id))
                .void

        override lazy val deleteAll: Z[Unit] =
          session
            .execute(Statement.Delete.all)
            .void

  private lazy val ChunkSizeInBytes: Int =
    1024
