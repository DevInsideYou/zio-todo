package dev.insideyou
package todo
package crud

import zio.*

final class BoundarySuite extends TestSuite:
  import BoundarySuite.*

  test("description should be trimmed"):
    val boundary =
      makeBoundary:
        new:
          override def createMany(todos: Vector[Todo.Data]): UIO[Vector[Todo.Existing[Unit]]] =
            ZIO.succeed:
              todos.map: data =>
                Todo.Existing((), data)

    forAll: (dataG: Todo.Data) =>
      val data =
        dataG.withUpdatedDescription(s"  ${dataG.description}  ")

      Unsafe.unsafely:
        Runtime
          .default
          .unsafe
          .run:
            boundary
              .createOne(data)
              .map: todo =>
                todo.description shouldBe data.description.trim

  test("readByDescription should not always call gate.readByDescription"):
    var wasCalled = false

    val boundary: Boundary[Any, Throwable, Unit] =
      makeBoundary:
        new:
          override def readManyByDescription(
            description: String
          ): UIO[Vector[Todo.Existing[Unit]]] =
            ZIO.succeed:
              wasCalled = true

              Vector.empty

    Unsafe.unsafely:
      Runtime
        .default
        .unsafe
        .run:
          for
            _ <- ZIO.succeed(When("the description is empty"))
            _ <- boundary.readManyByDescription("")
          yield
            Then("gate.readByDescription should NOT be called")
            wasCalled shouldBe false

    forAll(MinSuccessful(1)): (description: String) =>
      whenever(description.nonEmpty):
        Unsafe.unsafely:
          Runtime
            .default
            .unsafe
            .run:
              for
                _ <- ZIO.succeed(When("the description is NOT empty"))
                _ <- boundary.readManyByDescription(description)
              yield
                Then("gate.readByDescription should be called")
                wasCalled shouldBe true

  private def makeBoundary[TodoId](gate: FakeGate[TodoId]): Boundary[Any, Throwable, TodoId] =
    Boundary.make(gate)

object BoundarySuite:
  private class FakeGate[TodoId] extends Gate[Any, Nothing, TodoId]:
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
