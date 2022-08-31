package dev.insideyou
package todo
package crud

import zio.*

final class BoundarySuite extends TestSuite:
  import BoundarySuite.*

  test("readByDescription should not always call gate.readByDescription"):
    var wasCalled = false

    val boundary: Boundary[Any, Throwable, Unit] =
      makeBoundary:
        new:
          override def readManyByDescription(
              description: String
            ): UIO[Vector[Todo[Unit]]] =
            ZIO.succeed:
              wasCalled = true

              Vector.empty

    Runtime.default.unsafeRun:
      for
        _ <- ZIO.succeed(When("the description is empty"))
        _ <- boundary.readManyByDescription("")
      yield
        Then("gate.readByDescription should NOT be called")
        wasCalled `shouldBe` false

    forAll(MinSuccessful(1)) { (description: String) =>
      whenever(description.nonEmpty):
        Runtime.default.unsafeRun:
          for
            _ <- ZIO.succeed(When("the description is NOT empty"))
            _ <- boundary.readManyByDescription(description)
          yield
            Then("gate.readByDescription should be called")
            wasCalled `shouldBe` true
    }

  private def makeBoundary[TodoId](gate: FakeGate[TodoId]): Boundary[Any, Throwable, TodoId] =
    BoundaryImpl.make(gate)

object BoundarySuite:
  private class FakeGate[TodoId] extends Gate[Any, Nothing, TodoId]:
    override def updateMany(todos: Vector[Todo[TodoId]]): UIO[Vector[Todo[TodoId]]] = ???

    override def readManyById(ids: Vector[TodoId]): UIO[Vector[Todo[TodoId]]] = ???
    override def readManyByDescription(description: String): UIO[Vector[Todo[TodoId]]] = ???
    override def readAll: UIO[Vector[Todo[TodoId]]] = ???

    override def deleteMany(todos: Vector[Todo[TodoId]]): UIO[Unit] = ???
    override def deleteAll: UIO[Unit] = ???
