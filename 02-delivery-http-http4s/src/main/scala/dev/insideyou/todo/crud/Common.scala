package dev.insideyou
package todo
package crud

private def toLocalDateTime(input: String): Either[String, LocalDateTime] =
  val deadlinePromptPattern: String =
    "yyyy-M-d H:m"

  val formatter =
    DateTimeFormatter.ofPattern(deadlinePromptPattern)

  val trimmedInput: String =
    input.trim

  Either
    .catchNonFatal(LocalDateTime.parse(trimmedInput, formatter))
    .leftMap(_ => s"$trimmedInput does not match the required format $deadlinePromptPattern.")
