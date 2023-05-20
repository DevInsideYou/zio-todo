package dev.insideyou
package todo
package crud

import zio.{ Console as _, Random as _, * }

object DependencyGraph:
  def make(
    pattern: DateTimeFormatter,
    console: Console[Any, Nothing],
    random: Random[Any, Nothing],
    session: skunk.Session[Z],
  ): UIO[Controller[ZEnv, Nothing]] =
    PostgresGate
      .make(session)
      .map: gate =>
        Controller.make(
          pattern = pattern,
          boundary = Boundary.make(gate),
          console = FancyConsole.make(console),
          random = random,
        )
