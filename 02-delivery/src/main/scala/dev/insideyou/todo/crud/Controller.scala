package dev.insideyou
package todo
package crud

import scala.util.control.NonFatal

import cats.data.NonEmptyVector
import zio.*

trait Controller[-R, +E]:
  def program: ZIO[R, E, Unit]

object Controller:
  def make[R, TodoId](
      pattern: DateTimeFormatter,
      boundary: Boundary[R, Throwable, TodoId],
      console: FancyConsole[Any, Nothing],
      random: Random[Any, Nothing],
    )(using
      parse: Parse[String, TodoId]
    ): Controller[R, Nothing] =
    new:
      override lazy val program: URIO[R, Unit] =
        val colors: Vector[String] =
          Vector(
            // scala.Console.BLACK,
            scala.Console.BLUE,
            scala.Console.CYAN,
            scala.Console.GREEN,
            scala.Console.MAGENTA,
            scala.Console.RED,
            // scala.Console.WHITE,
            scala.Console.YELLOW,
          )

        val randomColor: UIO[String] =
          random.nextInt(colors.size).map(colors)

        val hyphens: UIO[String] =
          randomColor.map(inColor("─" * 100))

        val menu: UIO[String] =
          hyphens.map { h =>
            s"""|
                |$h
                |
                |c                   => create new todo
                |d                   => delete todo
                |da                  => delete all todos
                |sa                  => show all todos
                |sd                  => search by description
                |sid                 => search by id
                |ud                  => update description
                |udl                 => update deadline
                |e | q | exit | quit => exit the application
                |anything else       => show the main menu
                |
                |Please enter a command:""".stripMargin
          }

        val prompt: UIO[String] =
          menu.flatMap(console.getStrLnTrimmedWithPrompt)

        object Exit:
          def unapply(s: String): Boolean =
            Set("e", "q", "exit", "quit")(s)

        prompt
          .flatMap {
            case "c" => create.as(true)
            case "d" => delete.as(true)
            case "da" => deleteAll.as(true)
            case "sa" => showAll.as(true)
            case "sd" => searchByDescription.as(true)
            case "sid" => searchById.as(true)
            case "ud" => updateDescription.as(true)
            case "udl" => updateDeadline.as(true)
            case Exit() => exit.as(false)
            case _ => ZIO.succeed(true)
          }
          .catchAll {
            case NonFatal(throwable) =>
              console.putErrLn(throwable.getMessage).as(true)
          }
          .repeatWhile(identity)
          .unit
      end program

      private lazy val descriptionPrompt: UIO[String] =
        console.getStrLnTrimmedWithPrompt("Please enter a description:")

      private lazy val create: RIO[R, Unit] =
        descriptionPrompt.flatMap { description =>
          withDeadlinePrompt { deadline =>
            boundary.createOne(insert.Todo(description, deadline)) *>
              console.putSuccess("Successfully created the new todo.")
          }
        }

      private def withDeadlinePrompt(
          onSuccess: LocalDateTime => RIO[R, Unit]
        ): RIO[R, Unit] =
        deadlinePrompt
          .map(toLocalDateTime)
          .flatMap(_.fold(console.putErrLn, onSuccess))

      private lazy val deadlinePrompt: UIO[String] =
        console.getStrLnTrimmedWithPrompt(
          s"Please enter a deadline in the following format $DeadlinePromptFormat:"
        )

      private def toLocalDateTime(input: String): Either[String, LocalDateTime] =
        val formatter =
          DateTimeFormatter.ofPattern(DeadlinePromptPattern)

        val trimmedInput: String =
          input.trim

        Either
          .catchNonFatal(LocalDateTime.parse(trimmedInput, formatter))
          .leftMap { _ =>
            val renderedInput: String =
              inColor(trimmedInput)(scala.Console.YELLOW)

            s"\n$renderedInput does not match the required format $DeadlinePromptFormat."
          }

      private lazy val idPrompt: UIO[String] =
        console.getStrLnTrimmedWithPrompt("Please enter the id:")

      private lazy val delete: RIO[R, Unit] =
        withIdPrompt { id =>
          withReadOne(id) { todo =>
            boundary.deleteOne(todo) *>
              console.putSuccess("Successfully deleted the todo.")
          }
        }

      private def withIdPrompt(onValidId: TodoId => RIO[R, Unit]): RIO[R, Unit] =
        idPrompt.map(toId).flatMap(_.fold(console.putErrLn, onValidId))

      private def toId(userInput: String): Either[String, TodoId] =
        parse(userInput).leftMap(_.getMessage)

      private def withReadOne(
          id: TodoId
        )(
          onFound: Todo[TodoId] => RIO[R, Unit]
        ): RIO[R, Unit] =
        boundary
          .readOneById(id)
          .flatMap(_.fold(displayNoTodosFoundMessage)(onFound))

      private lazy val displayNoTodosFoundMessage: UIO[Unit] =
        console.putWarning("\nNo todos found!")

      private lazy val deleteAll: RIO[R, Unit] =
        boundary.deleteAll *> console.putSuccess("Successfully deleted all todos.")

      private lazy val showAll: RIO[R, Unit] =
        boundary
          .readAll
          .map(NonEmptyVector.fromVector)
          .flatMap(_.fold(displayNoTodosFoundMessage)(displayOneOrMany))

      private def displayOneOrMany(todos: NonEmptyVector[Todo[TodoId]]): UIO[Unit] =
        val uxMatters =
          if todos.size == 1 then "todo" else "todos"

        val renderedSize: String =
          inColor(todos.size.toString)(scala.Console.GREEN)

        console.putStrLn(s"\nFound $renderedSize $uxMatters:\n") *>
          todos
            .toVector
            .sortBy(_.deadline)
            .map(renderedWithPattern)
            .pipe(ZIO.foreach(_)(console.putStrLn))
            .unit

      private def renderedWithPattern(todo: Todo[TodoId]): String =
        val renderedId: String =
          inColor(todo.id.toString)(scala.Console.GREEN)

        val renderedDescription: String =
          inColor(todo.description)(scala.Console.MAGENTA)

        val renderedDeadline: String =
          inColor(todo.deadline.format(pattern))(scala.Console.YELLOW)

        s"$renderedId $renderedDescription is due on $renderedDeadline."

      private lazy val searchByDescription: RIO[R, Unit] =
        descriptionPrompt
          .flatMap(boundary.readManyByDescription)
          .map(NonEmptyVector.fromVector)
          .flatMap(_.fold(displayNoTodosFoundMessage)(displayOneOrMany))

      private lazy val searchById: RIO[R, Unit] =
        withIdPrompt { id =>
          withReadOne(id) { todo =>
            displayOneOrMany(NonEmptyVector.of(todo))
          }
        }

      private lazy val updateDescription: RIO[R, Unit] =
        withIdPrompt { id =>
          withReadOne(id) { todo =>
            descriptionPrompt.flatMap { description =>
              boundary.updateOne(todo.withUpdatedDescription(description)) *>
                console.putSuccess("Successfully updated the description.")
            }
          }
        }

      private lazy val updateDeadline: RIO[R, Unit] =
        withIdPrompt { id =>
          withReadOne(id) { todo =>
            withDeadlinePrompt { deadline =>
              boundary.updateOne(todo.withUpdatedDeadline(deadline)) *>
                console.putSuccess("Successfully updated the deadline.")
            }
          }
        }

      private lazy val exit: UIO[Unit] =
        console.putStrLn("\nUntil next time!\n")
    end new

  private lazy val DeadlinePromptPattern: String =
    "yyyy-M-d H:m"

  private lazy val DeadlinePromptFormat: String =
    inColor(DeadlinePromptPattern)(scala.Console.MAGENTA)

  private def inColor(line: String)(color: String): String =
    color + line + scala.Console.RESET
