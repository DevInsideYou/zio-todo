package dev.insideyou
package todo
package crud

import scala.util.control.NonFatal

import cats.*
import cats.data.*

trait Controller[F[_]]:
  def program: F[Unit]

object Controller:
  def make[F[_], TodoId](
      pattern: DateTimeFormatter,
      boundary: BoundaryOld[F, TodoId],
      console: FancyConsole[F],
      random: Random[F],
    )(using
      ME: MonadError[F, Throwable],
      parse: Parse[String, TodoId],
    ): Controller[F] =
    new:
      override lazy val program: F[Unit] =
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

        val randomColor: F[String] =
          random.nextInt(colors.size).map(colors)

        val hyphens: F[String] =
          randomColor.map(inColor("─" * 100))

        val menu: F[String] =
          hyphens.map { h =>
            s"""|
                |$h
                |
                |c                   => create new todo
                |d                   => delete todo
                |da                  => delete all todos
                |sa                  => show all todos
                |sd                  => search by partial description
                |sid                 => search by id
                |ud                  => update description
                |udl                 => update deadline
                |e | q | exit | quit => exit the application
                |anything else       => show the main menu
                |
                |Please enter a command:""".stripMargin
          }

        val prompt: F[String] =
          menu.flatMap(console.getStrLnTrimmedWithPrompt)

        object Exit:
          def unapply(s: String): Boolean =
            Set("e", "q", "exit", "quit")(s)

        prompt
          .flatMap {
            case "c"    => create.as(true)
            case "d"    => delete.as(true)
            case "da"   => deleteAll.as(true)
            case "sa"   => showAll.as(true)
            case "sd"   => searchByDescription.as(true)
            case "sid"  => searchById.as(true)
            case "ud"   => updateDescription.as(true)
            case "udl"  => updateDeadline.as(true)
            case Exit() => exit.as(false)
            case _      => true.pure[F]
          }
          .handleErrorWith {
            case NonFatal(throwable) =>
              console.putErrLn(throwable.getMessage).as(true)
          }
          .iterateWhile(identity)
          .void
      end program

      private lazy val descriptionPrompt: F[String] =
        console.getStrLnTrimmedWithPrompt("Please enter a description:")

      private lazy val create: F[Unit] =
        descriptionPrompt.flatMap { description =>
          withDeadlinePrompt { deadline =>
            boundary.createOne(Todo.Data(description, deadline)) >>
              console.putSuccess("Successfully created the new todo.")
          }
        }

      private def withDeadlinePrompt(
          onSuccess: LocalDateTime => F[Unit]
        ): F[Unit] =
        deadlinePrompt
          .map(toLocalDateTime)
          .flatMap(_.fold(console.putErrLn, onSuccess))

      private lazy val deadlinePrompt: F[String] =
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

      private lazy val idPrompt: F[String] =
        console.getStrLnTrimmedWithPrompt("Please enter the id:")

      private lazy val delete: F[Unit] =
        withIdPrompt { id =>
          withReadOne(id) { todo =>
            boundary.deleteOne(todo) >>
              console.putSuccess("Successfully deleted the todo.")
          }
        }

      private def withIdPrompt(onValidId: TodoId => F[Unit]): F[Unit] =
        idPrompt.map(toId).flatMap(_.fold(console.putErrLn, onValidId))

      private def toId(userInput: String): Either[String, TodoId] =
        parse(userInput).leftMap(_.getMessage)

      private def withReadOne(id: TodoId)(onFound: Todo.Existing[TodoId] => F[Unit]): F[Unit] =
        boundary
          .readOneById(id)
          .flatMap(_.fold(displayNoTodosFoundMessage)(onFound))

      private lazy val displayNoTodosFoundMessage: F[Unit] =
        console.putWarning("\nNo todos found!")

      private lazy val deleteAll: F[Unit] =
        boundary.deleteAll >> console.putSuccess("Successfully deleted all todos.")

      private lazy val showAll: F[Unit] =
        boundary
          .readAll
          .map(NonEmptyVector.fromVector)
          .flatMap(_.fold(displayNoTodosFoundMessage)(displayOneOrMany))

      private def displayOneOrMany(todos: NonEmptyVector[Todo.Existing[TodoId]]): F[Unit] =
        val uxMatters =
          if todos.size == 1 then "todo" else "todos"

        val renderedSize: String =
          inColor(todos.size.toString)(scala.Console.GREEN)

        console.putStrLn(s"\nFound $renderedSize $uxMatters:\n") >>
          todos
            .sortBy(_.deadline)(Order.fromOrdering)
            .map(renderedWithPattern)
            .traverse_(console.putStrLn)

      private def renderedWithPattern(todo: Todo.Existing[TodoId]): String =
        val renderedId: String =
          inColor(todo.id.toString)(scala.Console.GREEN)

        val renderedDescription: String =
          inColor(todo.description)(scala.Console.MAGENTA)

        val renderedDeadline: String =
          inColor(todo.deadline.format(pattern))(scala.Console.YELLOW)

        s"$renderedId $renderedDescription is due on $renderedDeadline."

      private lazy val searchByDescription: F[Unit] =
        descriptionPrompt
          .flatMap(boundary.readManyByPartialDescription)
          .map(NonEmptyVector.fromVector)
          .flatMap(_.fold(displayNoTodosFoundMessage)(displayOneOrMany))

      private lazy val searchById: F[Unit] =
        withIdPrompt { id =>
          withReadOne(id) { todo =>
            displayOneOrMany(NonEmptyVector.of(todo))
          }
        }

      private lazy val updateDescription: F[Unit] =
        withIdPrompt { id =>
          withReadOne(id) { todo =>
            descriptionPrompt.flatMap { description =>
              boundary.updateOne(todo.withUpdatedDescription(description)) >>
                console.putSuccess("Successfully updated the description.")
            }
          }
        }

      private lazy val updateDeadline: F[Unit] =
        withIdPrompt { id =>
          withReadOne(id) { todo =>
            withDeadlinePrompt { deadline =>
              boundary.updateOne(todo.withUpdatedDeadline(deadline)) >>
                console.putSuccess("Successfully updated the deadline.")
            }
          }
        }

      private lazy val exit: F[Unit] =
        console.putStrLn("\nUntil next time!\n")
    end new

  private lazy val DeadlinePromptPattern: String =
    "yyyy-M-d H:m"

  private lazy val DeadlinePromptFormat: String =
    inColor(DeadlinePromptPattern)(scala.Console.MAGENTA)

  private def inColor(line: String)(color: String): String =
    color + line + scala.Console.RESET
