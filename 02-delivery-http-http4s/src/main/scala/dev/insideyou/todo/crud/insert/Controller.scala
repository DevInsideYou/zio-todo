package dev.insideyou
package todo
package crud
package insert

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
    ZIO.succeed {
      new Controller with Http4sDsl[Z]:
        override lazy val routes: HttpRoutes[Z] =
          Router {
            "todos" -> HttpRoutes.of {
              case r @ POST -> Root => r.as[request.Todo.Create].flatMap(create)
            }
          }

        private def create(payload: request.Todo.Create): Z[ZResponse] =
          withDeadlinePrompt(payload.deadline) { deadline =>
            boundary
              .createOne(Todo(payload.description, deadline))
              .map(response.Todo(pattern))
              .map(_.asJson)
              .flatMap(Created(_))
          }

        private def withDeadlinePrompt(
            deadline: String
          )(
            onSuccess: LocalDateTime => Z[ZResponse]
          ): Z[ZResponse] =
          toLocalDateTime(deadline).fold(BadRequest(_), onSuccess)
    }

  object request:
    object Todo:
      final case class Create(description: String, deadline: String) derives Decoder

      given EntityDecoder[Z, Create] =
        jsonOf
