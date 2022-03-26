package dev.insideyou
package todo
package crud

final case class Todo[+TodoId](
    id: TodoId,
    description: String,
    deadline: LocalDateTime,
  ):
  def withUpdatedDescription(newDescription: String): Todo[TodoId] =
    copy(description = newDescription)

  def withUpdatedDeadline(newDeadline: LocalDateTime): Todo[TodoId] =
    copy(deadline = newDeadline)
