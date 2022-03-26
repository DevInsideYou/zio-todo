package dev.insideyou
package todo
package crud
package insert

import zio.*

object PostgresGate:
  def make(resource: RManaged[ZEnv, skunk.Session[Z]]): UIO[Gate[ZEnv, Throwable, UUID]] =
    ZIO.succeed {
      new:
        override def createMany(todos: Vector[Todo]): Z[Vector[crud.Todo[UUID]]] =
          todos.traverse(insertOne)

        private def insertOne(todo: Todo): Z[crud.Todo[UUID]] =
          resource.use { session =>
            session
              .prepare(Statement.Insert.one)
              .use { preparedQuery =>
                preparedQuery.unique(todo)
              }
          }
    }
