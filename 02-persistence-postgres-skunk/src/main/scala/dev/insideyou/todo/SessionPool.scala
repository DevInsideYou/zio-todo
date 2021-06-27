package dev.insideyou
package todo

import cats.effect.std
import cats.syntax.option.*
import natchez.Trace.Implicits.noop
import skunk.Session
import zio.*

object SessionPool:
  lazy val make: RManaged[ZEnv, RManaged[ZEnv, Session[Z]]] =
    given std.Console[Z] =
      std.Console.make

    Session
      .pooled(
        host = "localhost",
        port = 5432,
        user = "user",
        password = "password".some,
        database = "todo",
        max = 10,
        debug = false,
      )
      .toManagedZIO
      .map(_.toManagedZIO)
