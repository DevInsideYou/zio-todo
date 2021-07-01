package dev.insideyou
package todo
package crud

import zio.*

final class InMemoryEntityGatewaySuite extends TestSuite:
  test("what's written should be read") {
    forAll { (data1: Todo.Data, data2: Todo.Data) =>
      val program: Task[Assertion] =
        for
          entityGateway <- makeEntityGateway(existing = Vector.empty)
          written <- entityGateway.createMany(Vector(data1, data2))
          read <- entityGateway.readAll
        yield
          written `shouldBe` read

          read `shouldBe` Vector(Todo.Existing(0, data1), Todo.Existing(1, data2))

      Runtime.default.unsafeRun(program)
    }
  }

  test("update nonexisting should throw") {
    forAll { (existing: Todo.Existing[Int]) =>
      val program: Task[Unit] =
        for
          entityGateway <- makeEntityGateway(existing = Vector.empty)
          _ <- entityGateway.updateMany(Vector(existing))
          _ <- entityGateway.readAll
        yield ()

      noException `should` be `thrownBy` program

      the[RuntimeException] thrownBy {
        Runtime.default.unsafeRunTask(program)
      } `should` have `message` s"Failed to update todo: ${existing.id} because it didn't exist."
    }
  }

  private def makeEntityGateway(
      existing: Vector[Todo.Existing[Int]]
    ): UIO[EntityGateway[Any, Throwable, Int]] =
    Ref.make(existing).map(InMemoryEntityGateway.make)
