package dev.insideyou

given Parse[String, Int] =
  string =>
    Either.catchNonFatal(string.toInt).leftMap { cause =>
      IllegalArgumentException(s"""Attempt to convert "$string" to Int failed.""", cause)
    }
