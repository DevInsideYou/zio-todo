package dev.insideyou
package todo
package crud

import cats.*

object PostgresEntityGateway:
  def make[F[_]](
      resource: effect.Resource[F, skunk.Session[F]]
    )(using
      S: effect.Sync[F]
    ): F[EntityGatewayOld[F, UUID]] =
    S.delay {
      new:
        override def createMany(todos: Vector[Todo.Data]): F[Vector[Todo.Existing[UUID]]] =
          todos.traverse(insertOne)

        override def updateMany(
            todos: Vector[Todo.Existing[UUID]]
          ): F[Vector[Todo.Existing[UUID]]] =
          todos.traverse(updateOne)

        private def insertOne(data: Todo.Data): F[Todo.Existing[UUID]] =
          resource.use { session =>
            session
              .prepare(Statement.Insert.one)
              .use { preparedQuery =>
                preparedQuery.unique(data)
              }
          }

        private def updateOne(
            todo: Todo.Existing[UUID]
          ): F[Todo.Existing[UUID]] =
          resource.use { session =>
            session
              .prepare(Statement.Update.one)
              .use { preparedQuery =>
                preparedQuery.unique(todo)
              }
          }

        override def readManyById(
            ids: Vector[UUID]
          ): F[Vector[Todo.Existing[UUID]]] =
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

        override def readManyByPartialDescription(
            partialDescription: String
          ): F[Vector[Todo.Existing[UUID]]] =
          resource.use { session =>
            session
              .prepare(Statement.Select.byDescription)
              .use { preparedQuery =>
                preparedQuery
                  .stream(partialDescription, ChunkSizeInBytes)
                  .compile
                  .toVector
              }
          }

        override lazy val readAll: F[Vector[Todo.Existing[UUID]]] =
          resource.use { session =>
            session
              .execute(Statement.Select.all)
              .map(_.to(Vector))
          }

        override def deleteMany(todos: Vector[Todo.Existing[UUID]]): F[Unit] =
          resource.use { session =>
            session
              .prepare(Statement.Delete.many(todos.size))
              .use { preparedCommand =>
                preparedCommand
                  .execute(todos.to(List).map(_.id))
                  .void
              }
          }

        override lazy val deleteAll: F[Unit] =
          resource.use { session =>
            session
              .execute(Statement.Delete.all)
              .void
          }
    }

  private lazy val ChunkSizeInBytes: Int =
    1024
