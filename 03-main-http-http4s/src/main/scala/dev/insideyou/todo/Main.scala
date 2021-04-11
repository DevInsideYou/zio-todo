package dev.insideyou
package todo

import scala.concurrent.*

object Main extends App:
  val executionContext: ExecutionContext =
    ExecutionContext.global

  scala.util.Random.nextInt(2) match
    case 0 =>
      println(inColor("Running on cats.effect.IO")(scala.Console.RED))

      import cats.effect.unsafe.implicits.global

      Program.make[cats.effect.IO](executionContext).unsafeRunSync()

    case 1 =>
      println(inColor("Running on zio.Task")(scala.Console.CYAN))

      import zio.*
      import zio.interop.catz.*

      Runtime.default.unsafeRun(Program.make(executionContext))

    case _ =>
      println(inColor("Running on monix.eval.Task")(scala.Console.GREEN))

  // import monix.execution.Scheduler.Implicits.global

  // Program
  //   .make[monix.eval.Task](executionContext)
  //   .runSyncUnsafe(duration.Duration.Inf)

  private def inColor(line: String)(color: String): String =
    color + line + scala.Console.RESET
