package dev.insideyou
package todo
package crud
package insert

final case class Todo(
    description: String,
    deadline: LocalDateTime,
  ):
  def withUpdatedDescription(newDescription: String): Todo =
    copy(description = newDescription)

  def withUpdatedDeadline(newDeadline: LocalDateTime): Todo =
    copy(deadline = newDeadline)
