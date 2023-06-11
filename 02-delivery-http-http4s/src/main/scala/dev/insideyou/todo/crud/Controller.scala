package dev.insideyou
package todo
package crud

import cats.data.NonEmptyChain
import io.circe.*
import io.circe.generic.semiauto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import zio.*

object Controller:
  def make[TodoId](
    pattern: DateTimeFormatter,
    boundary: Boundary[ZEnv, Throwable, TodoId],
  )(using
    parse: Parse[String, TodoId]
  ): UIO[Controller] =
    ZIO.succeed:
      new Controller with Http4sDsl[Z]:
        override lazy val routes: HttpRoutes[Z] =
          Router:
            "todos" -> HttpRoutes.of:
              case r @ POST -> Root => r.as[request.Todo.Create].flatMap(create)
              case r @ PUT -> Root / id => r.as[request.Todo.Update].flatMap(update(id))

              case GET -> Root :? Description(d) => searchByDescription(d)
              case GET -> Root => Created("some change")
              case GET -> Root / id => searchById(id)

              case DELETE -> Root => deleteAll
              case DELETE -> Root / id => delete(id)

        object Description extends QueryParamDecoderMatcher[String]("description")

        private def create(payload: request.Todo.Create): Z[ZResponse] =
          withDeadlinePrompt(payload.deadline): deadline =>
            boundary
              .createOne(Todo.Data(payload.description, deadline))
              .map(response.Todo(pattern))
              .map(_.asJson)
              .flatMap(Created(_))

        private def withDeadlinePrompt(
          deadline: String
        )(
          onSuccess: LocalDateTime => Z[ZResponse]
        ): Z[ZResponse] =
          toLocalDateTime(deadline).fold(BadRequest(_), onSuccess)

        private def toLocalDateTime(input: String): Either[String, LocalDateTime] =
          val formatter =
            DateTimeFormatter.ofPattern(DeadlinePromptPattern)

          val trimmedInput: String =
            input.trim

          Either
            .catchNonFatal(LocalDateTime.parse(trimmedInput, formatter))
            .leftMap: _ =>
              s"$trimmedInput does not match the required format $DeadlinePromptPattern."

        private def update(id: String): request.Todo.Update => Z[ZResponse] =
          _.fold(updateDescription(id), updateDeadline(id), updateAllFields(id))

        private def updateDescription(id: String)(description: String): Z[ZResponse] =
          withIdPrompt(id): id =>
            withReadOne(id): todo =>
              boundary
                .updateOne(todo.withUpdatedDescription(description))
                .map(response.Todo(pattern))
                .map(_.asJson)
                .flatMap(Ok(_))

        private def updateDeadline(id: String)(deadline: String): Z[ZResponse] =
          withIdPrompt(id): id =>
            withDeadlinePrompt(deadline): deadline =>
              withReadOne(id): todo =>
                boundary
                  .updateOne(todo.withUpdatedDeadline(deadline))
                  .map(response.Todo(pattern))
                  .map(_.asJson)
                  .flatMap(Ok(_))

        private def updateAllFields(
          id: String
        )(
          description: String,
          deadline: String,
        ): Z[ZResponse] =
          (
            toId(id).toEitherNec,
            toLocalDateTime(deadline).toEitherNec,
          )
            .parTupled
            .fold(
              errors => BadRequest(errors.asJson),
              happyPath(description).tupled,
            )

        private def happyPath(
          description: String
        )(
          id: TodoId,
          deadline: LocalDateTime,
        ): Z[ZResponse] =
          withReadOne(id): todo =>
            boundary
              .updateOne:
                todo
                  .withUpdatedDescription(description)
                  .withUpdatedDeadline(deadline)
              .map(response.Todo(pattern))
              .map(_.asJson)
              .flatMap(Ok(_))

        @annotation.nowarn
        private lazy val showAll: Z[ZResponse] =
          boundary
            .readAll
            .flatMap: todos =>
              todos
                .sortBy(_.deadline)
                .map(response.Todo(pattern))
                .asJson
                .pipe(Ok(_))

        private def searchById(id: String): Z[ZResponse] =
          withIdPrompt(id): id =>
            withReadOne(id): todo =>
              todo
                .pipe(response.Todo(pattern))
                .pipe(_.asJson)
                .pipe(Ok(_))

        private def searchByDescription(description: String): Z[ZResponse] =
          boundary
            .readManyByDescription(description)
            .flatMap: todos =>
              todos
                .map(response.Todo(pattern))
                .asJson
                .pipe(Ok(_))

        private def delete(id: String): Z[ZResponse] =
          withIdPrompt(id): id =>
            withReadOne(id): todo =>
              boundary.deleteOne(todo) >>
                NoContent()

        private def withIdPrompt(id: String)(onValidId: TodoId => Z[ZResponse]): Z[ZResponse] =
          toId(id).fold(BadRequest(_), onValidId)

        private def toId(userInput: String): Either[String, TodoId] =
          parse(userInput).leftMap(_.getMessage)

        private def withReadOne(
          id: TodoId
        )(
          onFound: Todo.Existing[TodoId] => Z[ZResponse]
        ): Z[ZResponse] =
          boundary
            .readOneById(id)
            .flatMap(_.fold(displayNoTodosFoundMessage)(onFound))

        private lazy val displayNoTodosFoundMessage: Z[ZResponse] =
          NotFound("No todos found!")

        private lazy val deleteAll: Z[ZResponse] =
          boundary.deleteAll >> NoContent()

  object request:
    object Todo:
      final type Create = Update.AllFields
      final val Create: Update.AllFields.type = Update.AllFields

      given Decoder[Create] =
        deriveDecoder

      given EntityDecoder[Z, Create] =
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
            case Description(description) => ifDescription(description)
            case Deadline(deadline) => ifDeadline(deadline)
            case AllFields(description, deadline) => ifAllFields(description, deadline)

      object Update:
        given Decoder[Update] =
          NonEmptyChain[Decoder[Update]](
            deriveDecoder[AllFields].widen, // order matters
            deriveDecoder[Description].widen,
            deriveDecoder[Deadline].widen,
          ).reduceLeft(_ `or` _)

        given EntityDecoder[Z, Update] =
          jsonOf

  private lazy val DeadlinePromptPattern: String =
    "yyyy-M-d H:m"
