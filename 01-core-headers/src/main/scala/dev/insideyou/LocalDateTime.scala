package dev.insideyou

type LocalDateTime = java.time.LocalDateTime
object LocalDateTime:
  inline def parse(text: CharSequence, formatter: DateTimeFormatter): LocalDateTime =
    java.time.LocalDateTime.parse(text, formatter)
