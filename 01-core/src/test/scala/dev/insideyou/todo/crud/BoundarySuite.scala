package dev.insideyou
package todo
package crud

import zio.*

final class BoundarySuite extends TestSuite:
  import BoundarySuite.*

  test("description should be trimmed") {
    val entityGateway: EntityGateway[Unit, Any, Nothing] =
      new FakeEntityGateway[Unit]:
        override def createMany(todos: Vector[Todo.Data]): UIO[Vector[Todo.Existing[Unit]]] =
          ZIO.succeed {
            todos.map { data =>
              Todo.Existing((), data)
            }
          }

    val boundary: Boundary[Unit, Any, Throwable] =
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

    val entityGateway: EntityGateway[Unit, Any, Nothing] =
      new FakeEntityGateway[Unit]:
        override def readManyByPartialDescription(
            partialDescription: String
          ): UIO[Vector[Todo.Existing[Unit]]] =
          ZIO.succeed {
            wasCalled = true

            Vector.empty
          }

    val boundary: Boundary[Unit, Any, Throwable] =
      Boundary.make(entityGateway)

    Runtime.default.unsafeRun {
      for
        _ <- ZIO.succeed(When("the description is empty"))
        _ <- boundary.readManyByPartialDescription("")
      yield
        Then("gateway.readByDescription should NOT be called")
        wasCalled `shouldBe` false
    }

    forAll(MinSuccessful(1)) { (description: String) =>
      whenever(description.nonEmpty) {
        Runtime.default.unsafeRun {
          for
            _ <- ZIO.succeed(When("the description is NOT empty"))
            _ <- boundary.readManyByPartialDescription(description)
          yield
            Then("gateway.readByDescription should be called")
            wasCalled `shouldBe` true
        }
      }
    }
  }

object BoundarySuite:
  private class FakeEntityGateway[TodoId] extends EntityGateway[TodoId, Any, Nothing]:
    override def createMany(todos: Vector[Todo.Data]): UIO[Vector[Todo.Existing[TodoId]]] = ???

    override def updateMany(
        todos: Vector[Todo.Existing[TodoId]]
      ): UIO[Vector[Todo.Existing[TodoId]]] = ???

    override def readManyById(ids: Vector[TodoId]): UIO[Vector[Todo.Existing[TodoId]]] = ???

    override def readManyByPartialDescription(
        partialDescription: String
      ): UIO[Vector[Todo.Existing[TodoId]]] = ???

    override def readAll: UIO[Vector[Todo.Existing[TodoId]]] = ???

    override def deleteMany(todos: Vector[Todo.Existing[TodoId]]): UIO[Unit] = ???
    override def deleteAll: UIO[Unit] = ???
