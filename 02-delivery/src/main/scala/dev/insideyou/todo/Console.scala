package dev.insideyou
package todo

import zio.*

trait Console[-R, +E]:
  def getStrLnWithPrompt(prompt: String): ZIO[R, E, String]
  def putStrLn(line: String): ZIO[R, E, Unit]
  def putErrLn(line: String): ZIO[R, E, Unit]

object Console:
  lazy val make: UIO[Console[Any, Nothing]] =
    ZIO.succeed:
      new:
        override def getStrLnWithPrompt(prompt: String): UIO[String] =
          ZIO.succeed(scala.io.StdIn.readLine(prompt))

        override def putStrLn(line: String): UIO[Unit] =
          ZIO.succeed(println(line))

        override def putErrLn(line: String): UIO[Unit] =
          ZIO.succeed(scala.Console.err.println(line))
