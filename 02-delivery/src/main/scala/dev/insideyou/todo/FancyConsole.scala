package dev.insideyou
package todo

import zio.*

trait FancyConsole[-R, +E]:
  def getStrLnTrimmedWithPrompt(prompt: String): ZIO[R, E, String]
  def putStrLn(line: String): ZIO[R, E, Unit]
  def putSuccess(line: String): ZIO[R, E, Unit]
  def putWarning(line: String): ZIO[R, E, Unit]
  def putErrLn(line: String): ZIO[R, E, Unit]
  def putStrLnInColor(line: String)(color: String): ZIO[R, E, Unit]

object FancyConsole:
  def make(console: Console[Any, Nothing]): FancyConsole[Any, Nothing] =
    new:
      override def getStrLnTrimmedWithPrompt(prompt: String): UIO[String] =
        console.getStrLnWithPrompt(prompt + " ").map(_.trim)

      override def putStrLn(line: String): UIO[Unit] =
        console.putStrLn(line)

      override def putStrLnInColor(line: String)(color: String): UIO[Unit] =
        console.putStrLn(inColor(line)(color))

      private def inColor(line: String)(color: String): String =
        color + line + scala.Console.RESET

      override def putSuccess(line: String): UIO[Unit] =
        putStrLnInColor(line)(scala.Console.GREEN)

      override def putWarning(line: String): UIO[Unit] =
        putStrLnInColor(line)(scala.Console.YELLOW)

      override def putErrLn(line: String): UIO[Unit] =
        putStrLnInColor(line)(scala.Console.RED)
