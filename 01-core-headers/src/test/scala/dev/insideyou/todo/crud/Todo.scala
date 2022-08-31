package dev.insideyou
package todo
package crud

given [TodoId: Arbitrary]: Arbitrary[Todo[TodoId]] =
  Arbitrary:
    for
      todoId <- Arbitrary.arbitrary[TodoId]
      description <- Arbitrary.arbitrary[String]
      deadline <- Arbitrary.arbitrary[LocalDateTime]
    yield Todo(todoId, description, deadline)
