package dev.insideyou
package todo

import zio.*

object Main extends App:
  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    Program.make.exitCode
