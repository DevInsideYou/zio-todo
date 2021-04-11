package dev.insideyou

given [TodoId: Arbitrary]: Arbitrary[Todo.Existing[TodoId]] =
  Arbitrary {
    for
      todoId <- Arbitrary.arbitrary[TodoId]
      data <- Arbitrary.arbitrary[Todo.Data]
    yield Todo.Existing(todoId, data)
  }

given Arbitrary[Todo.Data] =
  Arbitrary {
    for
      description <- Arbitrary.arbitrary[String]
      deadline <- Arbitrary.arbitrary[LocalDateTime]
    yield Todo.Data(description, deadline)
  }
