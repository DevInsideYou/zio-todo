package dev.insideyou

type DateTimeFormatter = java.time.format.DateTimeFormatter
object DateTimeFormatter:
  inline def ofPattern(pattern: String): DateTimeFormatter =
    java.time.format.DateTimeFormatter.ofPattern(pattern)
