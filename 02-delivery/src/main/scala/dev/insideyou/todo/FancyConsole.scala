package dev.insideyou
package todo

import cats.*

trait FancyConsole[F[_]]:
  def getStrLnTrimmedWithPrompt(prompt: String): F[String]
  def putStrLn(line: String): F[Unit]
  def putSuccess(line: String): F[Unit]
  def putWarning(line: String): F[Unit]
  def putErrLn(line: String): F[Unit]
  def putStrLnInColor(line: String)(color: String): F[Unit]

object FancyConsole:
  def make[F[_]: Functor](console: Console[F]): FancyConsole[F] =
    new:
      override def getStrLnTrimmedWithPrompt(prompt: String): F[String] =
        console.getStrLnWithPrompt(prompt + " ").map(_.trim)

      override def putStrLn(line: String): F[Unit] =
        console.putStrLn(line)

      override def putStrLnInColor(line: String)(color: String): F[Unit] =
        console.putStrLn(inColor(line)(color))

      private def inColor(line: String)(color: String): String =
        color + line + scala.Console.RESET

      override def putSuccess(line: String): F[Unit] =
        putStrLnInColor(line)(scala.Console.GREEN)

      override def putWarning(line: String): F[Unit] =
        putStrLnInColor(line)(scala.Console.YELLOW)

      override def putErrLn(line: String): F[Unit] =
        putStrLnInColor(line)(scala.Console.RED)
