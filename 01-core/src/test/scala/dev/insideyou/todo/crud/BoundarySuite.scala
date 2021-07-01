package dev.insideyou
package todo
package crud

import zio.*

final class BoundarySuite extends TestSuite:
  import BoundarySuite.*

  test("description should be trimmed") {
    val entityGateway: EntityGateway[Any, Nothing, Unit] =
      new FakeEntityGateway[Unit]:
        override def createMany(todos: Vector[Todo.Data]): UIO[Vector[Todo.Existing[Unit]]] =
          ZIO.succeed {
            todos.map { data =>
              Todo.Existing((), data)
            }
          }

    val boundary: Boundary[Any, Throwable, Unit] =
      Boundary.make(entityGateway)

    forAll { (data: Todo.Data) =>
      Runtime.default.unsafeRun {
        boundary.createOne(data).map { todo =>
          todo.description `shouldBe` data.description.trim
        }
      }
    }
  }

  test("readByDescription should not always call gateway.readByDescription") {
    var wasCalled = false

    val entityGateway: EntityGateway[Any, Nothing, Unit] =
      new FakeEntityGateway[Unit]:
        override def readManyByDescription(description: String): UIO[Vector[Todo.Existing[Unit]]] =
          ZIO.succeed {
            wasCalled = true

            Vector.empty
          }

    val boundary: Boundary[Any, Throwable, Unit] =
      Boundary.make(entityGateway)

    Runtime.default.unsafeRun {
      for
        _ <- ZIO.succeed(When("the description is empty"))
        _ <- boundary.readManyByDescription("")
      yield
        Then("gateway.readByDescription should NOT be called")
        wasCalled `shouldBe` false
    }

    forAll(MinSuccessful(1)) { (description: String) =>
      whenever(description.nonEmpty) {
        Runtime.default.unsafeRun {
          for
            _ <- ZIO.succeed(When("the description is NOT empty"))
            _ <- boundary.readManyByDescription(description)
          yield
            Then("gateway.readByDescription should be called")
            wasCalled `shouldBe` true
        }
      }
    }
  }

object BoundarySuite:
  private class FakeEntityGateway[TodoId] extends EntityGateway[Any, Nothing, TodoId]:
    override def createMany(todos: Vector[Todo.Data]): UIO[Vector[Todo.Existing[TodoId]]] = ???

    override def updateMany(
        todos: Vector[Todo.Existing[TodoId]]
      ): UIO[Vector[Todo.Existing[TodoId]]] = ???

    override def readManyById(ids: Vector[TodoId]): UIO[Vector[Todo.Existing[TodoId]]] = ???
    override def readManyByDescription(description: String): UIO[Vector[Todo.Existing[TodoId]]] =
      ???

    override def readAll: UIO[Vector[Todo.Existing[TodoId]]] = ???

    override def deleteMany(todos: Vector[Todo.Existing[TodoId]]): UIO[Unit] = ???
    override def deleteAll: UIO[Unit] = ???
