package dev.insideyou
package todo

object MainOld extends App:
  scala.util.Random.nextInt(2) match
    case 0 =>
      println(inColor("Running on cats.effect.IO")(scala.Console.RED))

      import cats.effect.unsafe.implicits.global

      ProgramOld.make[cats.effect.IO].unsafeRunSync()

    case 1 =>
      println(inColor("Running on zio.Task")(scala.Console.CYAN))

      import zio.*
      import zio.interop.catz.*

      Runtime.default.unsafeRun(ProgramOld.make)

    case _ =>
      println(inColor("Running on monix.eval.Task")(scala.Console.GREEN))

  // import monix.execution.Scheduler.Implicits.global

  // Program.make[monix.eval.Task].runSyncUnsafe(duration.Duration.Inf)

  private def inColor(line: String)(color: String): String =
    color + line + scala.Console.RESET
