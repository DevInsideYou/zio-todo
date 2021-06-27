package dev.insideyou
package todo

import cats.*

trait Console[F[_]]:
  def getStrLnWithPrompt(prompt: String): F[String]
  def putStrLn(line: String): F[Unit]
  def putErrLn(line: String): F[Unit]

object Console:
  def make[F[_]](using S: effect.Sync[F]): F[Console[F]] =
    S.delay {
      new:
        override def getStrLnWithPrompt(prompt: String): F[String] =
          S.delay(scala.io.StdIn.readLine(prompt))

        override def putStrLn(line: String): F[Unit] =
          S.delay(println(line))

        override def putErrLn(line: String): F[Unit] =
          S.delay(scala.Console.err.println(line))
    }