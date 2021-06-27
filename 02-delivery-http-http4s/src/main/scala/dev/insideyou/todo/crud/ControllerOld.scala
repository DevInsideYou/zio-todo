package dev.insideyou
package todo
package crud

import cats.*
import cats.data.NonEmptyChain
import io.circe.*
import io.circe.generic.semiauto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

object ControllerOld:
  def make[F[_], TodoId](
      pattern: DateTimeFormatter,
      boundary: BoundaryOld[F, TodoId],
    )(using
      A: effect.Async[F],
      parse: Parse[String, TodoId],
    ): F[ControllerOld[F]] =
    A.delay {
      new ControllerOld[F] with Http4sDsl[F]:
        override lazy val routes: HttpRoutes[F] =
          Router {
            "todos" -> HttpRoutes.of {
              case r @ POST -> Root     => r.as[request.Todo.Create].flatMap(create)
              case r @ PUT -> Root / id => r.as[request.Todo.Update].flatMap(update(id))

              case GET -> Root :? Description(d) => searchByDescription(d)
              case GET -> Root                   => showAll
              case GET -> Root / id              => searchById(id)

              case DELETE -> Root      => deleteAll
              case DELETE -> Root / id => delete(id)
            }
          }

        object Description extends QueryParamDecoderMatcher[String]("description")

        private def create(payload: request.Todo.Create): F[Response[F]] =
          withDeadlinePrompt(payload.deadline) { deadline =>
            boundary
              .createOne(Todo.Data(payload.description, deadline))
              .map(response.Todo(pattern))
              .map(_.asJson)
              .flatMap(Created(_))
          }

        private def withDeadlinePrompt(
            deadline: String
          )(
            onSuccess: LocalDateTime => F[Response[F]]
          ): F[Response[F]] =
          toLocalDateTime(deadline).fold(BadRequest(_), onSuccess)

        private def toLocalDateTime(input: String): Either[String, LocalDateTime] =
          val formatter =
            DateTimeFormatter.ofPattern(DeadlinePromptPattern)

          val trimmedInput: String =
            input.trim

          Either
            .catchNonFatal(LocalDateTime.parse(trimmedInput, formatter))
            .leftMap { _ =>
              s"$trimmedInput does not match the required format $DeadlinePromptPattern."
            }

        private def update(id: String): request.Todo.Update => F[Response[F]] =
          _.fold(updateDescription(id), updateDeadline(id), updateAllFields(id))

        private def updateDescription(id: String)(description: String): F[Response[F]] =
          withIdPrompt(id) { id =>
            withReadOne(id) { todo =>
              boundary
                .updateOne(todo.withUpdatedDescription(description))
                .map(response.Todo(pattern))
                .map(_.asJson)
                .flatMap(Ok(_))
            }
          }

        private def updateDeadline(id: String)(deadline: String): F[Response[F]] =
          withIdPrompt(id) { id =>
            withDeadlinePrompt(deadline) { deadline =>
              withReadOne(id) { todo =>
                boundary
                  .updateOne(todo.withUpdatedDeadline(deadline))
                  .map(response.Todo(pattern))
                  .map(_.asJson)
                  .flatMap(Ok(_))
              }
            }
          }

        private def updateAllFields(
            id: String
          )(
            description: String,
            deadline: String,
          ): F[Response[F]] =
          (
            toId(id).toEitherNec,
            toLocalDateTime(deadline).toEitherNec,
          ).parTupled
            .fold(
              errors => BadRequest(errors.asJson),
              happyPath(description).tupled,
            )

        private def happyPath(
            description: String
          )(
            id: TodoId,
            deadline: LocalDateTime,
          ): F[Response[F]] =
          withReadOne(id) { todo =>
            boundary
              .updateOne(
                todo
                  .withUpdatedDescription(description)
                  .withUpdatedDeadline(deadline)
              )
              .map(response.Todo(pattern))
              .map(_.asJson)
              .flatMap(Ok(_))
          }

        private lazy val showAll: F[Response[F]] =
          boundary.readAll.flatMap { todos =>
            todos
              .sortBy(_.deadline)
              .map(response.Todo(pattern))
              .asJson
              .pipe(Ok(_))
          }

        private def searchById(id: String): F[Response[F]] =
          withIdPrompt(id) { id =>
            withReadOne(id) { todo =>
              todo
                .pipe(response.Todo(pattern))
                .pipe(_.asJson)
                .pipe(Ok(_))
            }
          }

        private def searchByDescription(description: String): F[Response[F]] =
          boundary.readManyByPartialDescription(description).flatMap { todos =>
            todos
              .map(response.Todo(pattern))
              .asJson
              .pipe(Ok(_))
          }

        private def delete(id: String): F[Response[F]] =
          withIdPrompt(id) { id =>
            withReadOne(id) { todo =>
              boundary.deleteOne(todo) >>
                NoContent()
            }
          }

        private def withIdPrompt(id: String)(onValidId: TodoId => F[Response[F]]): F[Response[F]] =
          toId(id).fold(BadRequest(_), onValidId)

        private def toId(userInput: String): Either[String, TodoId] =
          parse(userInput).leftMap(_.getMessage)

        private def withReadOne(
            id: TodoId
          )(
            onFound: Todo.Existing[TodoId] => F[Response[F]]
          ): F[Response[F]] =
          boundary
            .readOneById(id)
            .flatMap(_.fold(displayNoTodosFoundMessage)(onFound))

        private lazy val displayNoTodosFoundMessage: F[Response[F]] =
          NotFound("No todos found!")

        private lazy val deleteAll: F[Response[F]] =
          boundary.deleteAll >> NoContent()
    }

  object request:
    object Todo:
      final type Create = Update.AllFields
      final val Create: Update.AllFields.type = Update.AllFields

      given Decoder[Create] =
        deriveDecoder

      given [F[_]: effect.Concurrent]: EntityDecoder[F, Create] =
        jsonOf

      enum Update:
        case Description(description: String)
        case Deadline(deadline: String)
        case AllFields(description: String, deadline: String)

        final def fold[B](
            ifDescription: String => B,
            ifDeadline: String => B,
            ifAllFields: (String, String) => B,
          ): B =
          this match
            case Description(description)         => ifDescription(description)
            case Deadline(deadline)               => ifDeadline(deadline)
            case AllFields(description, deadline) => ifAllFields(description, deadline)

      object Update:
        given Decoder[Update] =
          NonEmptyChain[Decoder[Update]](
            deriveDecoder[AllFields].widen, // order matters
            deriveDecoder[Description].widen,
            deriveDecoder[Deadline].widen,
          ).reduceLeft(_ `or` _)

        given [F[_]: effect.Concurrent]: EntityDecoder[F, Update] =
          jsonOf

  private lazy val DeadlinePromptPattern: String =
    "yyyy-M-d H:m"
