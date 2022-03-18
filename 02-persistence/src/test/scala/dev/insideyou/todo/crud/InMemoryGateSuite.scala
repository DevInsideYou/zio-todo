package dev.insideyou
package todo
package crud

import zio.*

final class InMemoryGateSuite extends TestSuite:
  test("what's written should be read") {
    forAll { (data1: Todo.Data, data2: Todo.Data) =>
      val program: Task[Assertion] =
        for
          gate <- makeGate(existing = Vector.empty)
          written <- gate.createMany(Vector(data1, data2))
          read <- gate.readAll
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
          gate <- makeGate(existing = Vector.empty)
          _ <- gate.updateMany(Vector(existing))
          _ <- gate.readAll
        yield ()

      noException `should` be `thrownBy` program

      the[RuntimeException] thrownBy {
        Runtime.default.unsafeRunTask(program)
      } `should` have `message` s"Failed to update todo: ${existing.id} because it didn't exist."
    }
  }

  private def makeGate(
      existing: Vector[Todo.Existing[Int]]
    ): UIO[Gate[Any, Throwable, Int]] =
    Ref.make(existing).map(InMemoryGate.make)
