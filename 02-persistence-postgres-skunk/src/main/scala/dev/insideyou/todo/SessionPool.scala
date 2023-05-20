package dev.insideyou
package todo

import cats.effect.std
import cats.syntax.option.*
import natchez.Trace.Implicits.noop
import skunk.Session
import zio.*

object SessionPool:
  @annotation.nowarn("cat=deprecation")
  lazy val make =
    given std.Console[Z] =
      std.Console.make

    Session
      .pooled[Z](
        host = "localhost",
        port = 5432,
        user = "user",
        password = "password".some,
        database = "todo",
        max = 10,
        debug = false,
      )
      .toManagedZIO
      .flatMap(_.toManagedZIO)
