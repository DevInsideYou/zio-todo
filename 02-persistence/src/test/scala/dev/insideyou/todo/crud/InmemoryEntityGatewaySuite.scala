package dev.insideyou
package todo
package crud

import cats.*
import cats.effect.Ref
import cats.effect.unsafe.implicits.global

final class InMemoryEntityGatewaySuite extends TestSuite:
  private type F[+A] = effect.IO[A]
  // private val F = effect.IO

  test("what's written should be read") {
    forAll { (data1: Todo.Data, data2: Todo.Data) =>
      val program: F[Assertion] =
        for
          entityGateway <- makeEntityGateway(existing = Vector.empty)
          written <- entityGateway.createMany(Vector(data1, data2))
          read <- entityGateway.readAll
        yield
          written `shouldBe` read

          read `shouldBe` Vector(Todo.Existing(0, data1), Todo.Existing(1, data2))

      program.unsafeRunSync()
    }
  }

  test("update nonexisting should throw") {
    forAll { (existing: Todo.Existing[Int]) =>
      val program: F[Unit] =
        for
          entityGateway <- makeEntityGateway(existing = Vector.empty)
          _ <- entityGateway.updateMany(Vector(existing))
          _ <- entityGateway.readAll
        yield ()

      noException `should` be `thrownBy` program

      the[RuntimeException] thrownBy {
        program.unsafeRunSync()
      } `should` have `message` s"Failed to update todo: ${existing.id} because it didn't exist."
    }
  }

  private def makeEntityGateway(existing: Vector[Todo.Existing[Int]]): F[EntityGatewayOld[F, Int]] =
    Ref.of[F, Vector[Todo.Existing[Int]]](existing).map(InMemoryEntityGateway.make[F])
