package dev.insideyou
package todo
package crud
package insert

given Arbitrary[Todo] =
  Arbitrary {
    for
      description <- Arbitrary.arbitrary[String]
      deadline <- Arbitrary.arbitrary[LocalDateTime]
    yield Todo(description, deadline)
  }
