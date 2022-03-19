package dev.insideyou
package todo
package crud

import zio.*

final class InMemoryGateSuite extends TestSuite:
  test("what's written should be read") {
    import insert.given

    forAll { (data1: insert.Todo, data2: insert.Todo) =>
      val program: Task[Assertion] =
        for
          gate <- makeGate(existing = Vector.empty)
          written <- gate.createMany(Vector(data1, data2))
          read <- gate.readAll
        yield
          written `shouldBe` read

          read `shouldBe` Vector(
            Todo(0, data1.description, data1.deadline),
            Todo(1, data2.description, data2.deadline),
          )

      Runtime.default.unsafeRun(program)
    }
  }

  test("update nonexisting should throw") {
    forAll { (existing: Todo[Int]) =>
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
      existing: Vector[Todo[Int]]
    ): UIO[Gate[Any, Throwable, Int]] =
    Ref.make(existing).map(InMemoryGate.make)
