package dev.insideyou
package todo
package crud

import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME as Pattern

import zio.*

final class ControllerSuite extends TestSuite:
  import ControllerSuite.{ *, given }

  test("test suite should quit automatically") {
    val boundary: Boundary[Any, Throwable, Unit] =
      new FakeBoundary[Unit]

    assert(
      boundary,
      input = List.empty,
      expectedOutput = Vector.empty,
    )
  }

  test("should create on 'c'") {
    val boundary: Boundary[Any, Throwable, Unit] =
      new FakeBoundary[Unit]:
        override def createOne(todo: Todo.Data): Task[Todo.Existing[Unit]] =
          ZIO.succeed(Todo.Existing((), todo))

    assert(
      boundary,
      input = List("c", "Invent time-travel!", "1955-11-5 18:00"),
      expectedOutput = Vector("Successfully created the new todo."),
    )
  }

  test("should keep running on error") {
    val boundary: Boundary[Any, Throwable, Unit] =
      new FakeBoundary[Unit]:
        override def createOne(todo: Todo.Data): Task[Todo.Existing[Unit]] =
          ZIO.fail(RuntimeException("boom"))

    forAll { (description: String) =>
      assert(
        boundary,
        input = List("c", description, "1955-11-5 18:00"),
        expectedOutput = Vector.empty,
        expectedErrors = Vector("boom"),
      )
    }
  }

  test("should yield an error if deadline does not match the required format") {
    val boundary: Boundary[Any, Throwable, Unit] =
      new FakeBoundary[Unit]

    forAll { (description: String, deadline: String) =>
      import scala.Console.*

      assert(
        boundary,
        input = List("c", description, deadline),
        expectedOutput = Vector.empty,
        expectedErrors = Vector(
          s"\n$YELLOW${deadline.trim}$RESET does not match the required format ${MAGENTA}yyyy-M-d H:m$RESET."
        ),
      )
    }
  }

  private def assert[TodoId](
      boundary: Boundary[Any, Throwable, TodoId],
      input: List[String],
      expectedOutput: Vector[String],
      expectedErrors: Vector[String] = Vector.empty,
    )(using
      parse: Parse[String, TodoId]
    ): Assertion =
    Runtime.default.unsafeRun {
      for
        ref <- Ref.make(UsefulConsole.State(input :+ "q"))
        controller = Controller.make(
          Pattern,
          boundary,
          UsefulConsole(ref),
          UsefulRandom(fakeN = 5),
        )
        _ <- controller.program
        state <- ref.get
      yield
        state.output `shouldBe` (expectedOutput :+ "\nUntil next time!\n")
        state.errors `shouldBe` expectedErrors
    }

object ControllerSuite:
  private class FakeBoundary[TodoId] extends Boundary[Any, Throwable, TodoId]:
    override def createOne(todo: Todo.Data): Task[Todo.Existing[TodoId]] = ???

    override def createMany(todos: Vector[Todo.Data]): Task[Vector[Todo.Existing[TodoId]]] = ???

    override def readOneById(id: TodoId): Task[Option[Todo.Existing[TodoId]]] = ???
    override def readManyById(ids: Vector[TodoId]): Task[Vector[Todo.Existing[TodoId]]] = ???
    override def readManyByDescription(description: String): Task[Vector[Todo.Existing[TodoId]]] =
      ???

    override def readAll: Task[Vector[Todo.Existing[TodoId]]] = ???

    override def updateOne(todo: Todo.Existing[TodoId]): Task[Todo.Existing[TodoId]] = ???

    override def updateMany(
        todos: Vector[Todo.Existing[TodoId]]
      ): Task[Vector[Todo.Existing[TodoId]]] = ???

    override def deleteOne(todo: Todo.Existing[TodoId]): Task[Unit] = ???
    override def deleteMany(todos: Vector[Todo.Existing[TodoId]]): Task[Unit] = ???
    override def deleteAll: Task[Unit] = ???

  private class FakeFancyConsole extends FancyConsole[Any, Nothing]:
    override def getStrLnTrimmedWithPrompt(prompt: String): UIO[String] = ???
    override def putStrLn(line: String): UIO[Unit] = ???
    override def putSuccess(line: String): UIO[Unit] = ???
    override def putWarning(line: String): UIO[Unit] = ???
    override def putErrLn(line: String): UIO[Unit] = ???
    override def putStrLnInColor(line: String)(color: String): UIO[Unit] = ???

  private class FakeRandom extends Random[Any, Nothing]:
    override def nextInt(n: Int): UIO[Int] = ???

  private given Parse[String, Unit] =
    _ => Right(())

  private class UsefulRandom(fakeN: Int) extends FakeRandom:
    override def nextInt(n: Int): UIO[Int] =
      ZIO.succeed(fakeN)

  private class UsefulConsole(
      ref: Ref[UsefulConsole.State]
    ) extends FakeFancyConsole:
    override def getStrLnTrimmedWithPrompt(prompt: String): UIO[String] =
      ref.modify { state =>
        val head :: tail = state.input: @unchecked

        head -> state.copy(input = tail)
      }

    override def putStrLn(line: String): UIO[Unit] =
      ref.update { state =>
        state.copy(output = state.output :+ line)
      }

    override def putSuccess(line: String): UIO[Unit] =
      putStrLn(line)

    override def putErrLn(line: String): UIO[Unit] =
      ref.update { state =>
        state.copy(errors = state.errors :+ line)
      }

  object UsefulConsole:
    final case class State(
        input: List[String],
        output: Vector[String] = Vector.empty,
        errors: Vector[String] = Vector.empty,
      )
