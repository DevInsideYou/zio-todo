package dev.insideyou
package todo
package crud

enum Todo[+TodoId]:
  case Existing(id: TodoId, data: Data)
  case Data(description: String, deadline: LocalDateTime) extends Todo[Nothing]

  def withUpdatedDescription(newDescription: String): this.type =
    this match
      case e: Existing[TodoId] =>
        e.copy(data = e.data.withUpdatedDescription(newDescription)).asInstanceOf[this.type]

      case d: Data =>
        d.copy(description = newDescription).asInstanceOf[this.type]

  def withUpdatedDeadline(newDeadline: LocalDateTime): this.type =
    this match
      case e: Existing[TodoId] =>
        e.copy(data = e.data.withUpdatedDeadline(newDeadline)).asInstanceOf[this.type]

      case d: Data =>
        d.copy(deadline = newDeadline).asInstanceOf[this.type]

object Todo:
  extension [TodoId](existing: Existing[TodoId])
    def description: String =
      existing.data.description

    def deadline: LocalDateTime =
      existing.data.deadline
