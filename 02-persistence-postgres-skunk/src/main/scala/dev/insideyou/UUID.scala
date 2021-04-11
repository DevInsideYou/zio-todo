package dev.insideyou

type UUID = java.util.UUID
object UUID:
  inline def randomUUID(): UUID =
    java.util.UUID.randomUUID()

  inline def fromString(name: String): UUID =
    java.util.UUID.fromString(name)
