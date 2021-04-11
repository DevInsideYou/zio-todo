package dev.insideyou

given Parse[String, UUID] =
  string => Either.catchNonFatal(UUID.fromString(string))
