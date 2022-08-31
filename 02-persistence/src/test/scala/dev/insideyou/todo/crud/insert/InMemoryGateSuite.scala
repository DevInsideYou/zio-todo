package dev.insideyou
package todo
package crud
package insert

import zio.*

final class InMemoryGateSuite extends TestSuite:
  test("what's written should be read"):
    forAll { (todo1: Todo, todo2: Todo) =>
      val program: Task[Assertion] =
        for
          (gate, crudGate) <- makeGates(existing = Vector.empty)
          written <- gate.createMany(Vector(todo1, todo2))
          read <- crudGate.readAll
        yield
          written `shouldBe` read

          read `shouldBe` Vector(
            crud.Todo(0, todo1.description, todo1.deadline),
            crud.Todo(1, todo2.description, todo2.deadline),
          )

      Runtime.default.unsafeRun(program)
    }

  private def makeGates(
      existing: Vector[crud.Todo[Int]]
    ): UIO[(Gate[Any, Throwable, Int], crud.Gate[Any, Throwable, Int])] =
    Ref.make(existing).map { state =>
      InMemoryGate.make(state) -> crud.InMemoryGate.make(state)
    }
