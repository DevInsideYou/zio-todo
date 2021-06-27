package dev.insideyou
package todo
package crud

import cats.*

final class BoundarySuiteOld extends TestSuite:
  import BoundarySuiteOld.*

  private type F[A] = Id[A]

  test("description should be trimmed") {
    val entityGateway: EntityGatewayOld[F, Unit] =
      new FakeEntityGateway[F, Unit]:
        override def createMany(todos: Vector[Todo.Data]): F[Vector[Todo.Existing[Unit]]] =
          todos.map { data =>
            Todo.Existing((), data)
          }

    val boundary: BoundaryOld[F, Unit] =
      BoundaryOld.make(entityGateway)

    forAll { (data: Todo.Data) =>
      boundary.createOne(data).description `shouldBe` data.description.trim
    }
  }

  test("readByDescription should not always call gateway.readByDescription") {
    var wasCalled = false

    val entityGateway: EntityGatewayOld[F, Unit] =
      new FakeEntityGateway[F, Unit]:
        override def readManyByPartialDescription(
            partialDescription: String
          ): F[Vector[Todo.Existing[Unit]]] =
          wasCalled = true

          Vector.empty

    val boundary: BoundaryOld[F, Unit] =
      BoundaryOld.make(entityGateway)

    When("the description is empty")
    boundary.readManyByPartialDescription("")

    Then("gateway.readByDescription should NOT be called")
    wasCalled `shouldBe` false

    forAll(MinSuccessful(1)) { (description: String) =>
      whenever(description.nonEmpty) {
        When("the description is NOT empty")
        boundary.readManyByPartialDescription(description)

        Then("gateway.readByDescription should be called")
        wasCalled `shouldBe` true
      }
    }
  }

object BoundarySuiteOld:
  private class FakeEntityGateway[F[_], TodoId] extends EntityGatewayOld[F, TodoId]:
    override def createMany(todos: Vector[Todo.Data]): F[Vector[Todo.Existing[TodoId]]] = ???

    override def updateMany(
        todos: Vector[Todo.Existing[TodoId]]
      ): F[Vector[Todo.Existing[TodoId]]] = ???

    override def readManyById(ids: Vector[TodoId]): F[Vector[Todo.Existing[TodoId]]] = ???

    override def readManyByPartialDescription(
        partialDescription: String
      ): F[Vector[Todo.Existing[TodoId]]] = ???

    override def readAll: F[Vector[Todo.Existing[TodoId]]] = ???

    override def deleteMany(todos: Vector[Todo.Existing[TodoId]]): F[Unit] = ???
    override def deleteAll: F[Unit] = ???
