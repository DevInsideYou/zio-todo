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
        host = sys.env.getOrElse("POSTGRES_HOST", "localhost"),
        port = sys.env.getOrElse("POSTGRES_PORT", "5432").toInt,
        user = sys.env.getOrElse("POSTGRES_USER", "user"),
        password = sys.env.getOrElse("POSTGRES_PASSWORD", "password").some,
        database = sys.env.getOrElse("POSTGRES_DATABASE", "todo"),
        max = 10,
        debug = false,
      )
      .toManagedZIO
      .flatMap(_.toManagedZIO)
