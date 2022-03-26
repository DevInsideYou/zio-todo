package dev.insideyou
package todo
package crud

import zio.*

object PostgresGate:
  def make(resource: RManaged[ZEnv, skunk.Session[Z]]): UIO[Gate[ZEnv, Throwable, UUID]] =
    ZIO.succeed {
      new:
        override def updateMany(todos: Vector[Todo[UUID]]): Z[Vector[Todo[UUID]]] =
          todos.traverse(updateOne)

        private def updateOne(todo: Todo[UUID]): Z[Todo[UUID]] =
          resource.use { session =>
            session
              .prepare(Statement.Update.one)
              .use { preparedQuery =>
                preparedQuery.unique(todo)
              }
          }

        override def readManyById(ids: Vector[UUID]): Z[Vector[Todo[UUID]]] =
          resource.use { session =>
            session
              .prepare(Statement.Select.many(ids.size))
              .use { preparedQuery =>
                preparedQuery
                  .stream(ids.to(List), ChunkSizeInBytes)
                  .compile
                  .toVector
              }
          }

        override def readManyByDescription(description: String): Z[Vector[Todo[UUID]]] =
          resource.use { session =>
            session
              .prepare(Statement.Select.byDescription)
              .use { preparedQuery =>
                preparedQuery
                  .stream(description, ChunkSizeInBytes)
                  .compile
                  .toVector
              }
          }

        override lazy val readAll: Z[Vector[Todo[UUID]]] =
          resource.use { session =>
            session
              .execute(Statement.Select.all)
              .map(_.to(Vector))
          }

        override def deleteMany(todos: Vector[Todo[UUID]]): Z[Unit] =
          resource.use { session =>
            session
              .prepare(Statement.Delete.many(todos.size))
              .use { preparedCommand =>
                preparedCommand
                  .execute(todos.to(List).map(_.id))
                  .void
              }
          }

        override lazy val deleteAll: Z[Unit] =
          resource.use { session =>
            session
              .execute(Statement.Delete.all)
              .void
          }
    }

  private lazy val ChunkSizeInBytes: Int =
    1024
