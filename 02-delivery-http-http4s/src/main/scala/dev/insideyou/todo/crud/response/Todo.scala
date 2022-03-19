package dev.insideyou
package todo
package crud
package response

import io.circe.*
import io.circe.generic.semiauto.*
import org.http4s.*
import org.http4s.circe.*

final case class Todo(
    id: String,
    description: String,
    deadline: String,
  )

object Todo:
  def apply[TodoId](pattern: DateTimeFormatter)(existing: crud.Todo[TodoId]): Todo =
    Todo(
      id = existing.id.toString,
      description = existing.description,
      deadline = existing.deadline.format(pattern),
    )

  given Encoder[Todo] =
    deriveEncoder

  given EntityEncoder[Z, Todo] =
    jsonEncoderOf
