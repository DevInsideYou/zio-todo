package dev.insideyou
package todo
package crud

import zio.*

final class InMemoryGateSuite extends TestSuite:
  test("update nonexisting should throw"):
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

  private def makeGate(existing: Vector[Todo[Int]]): UIO[Gate[Any, Throwable, Int]] =
    Ref.make(existing).map(InMemoryGate.make)
