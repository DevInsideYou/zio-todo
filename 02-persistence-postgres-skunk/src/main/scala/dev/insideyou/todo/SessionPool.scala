package dev.insideyou
package todo

import cats.effect.*
import cats.syntax.option.*
import fs2.io.net.Network
import skunk.*

object SessionPool:
  def make[F[_]: Concurrent: std.Console: Network: natchez.Trace]: SessionPool[F] =
    Session.pooled(
      host = "localhost",
      port = 5432,
      user = "user",
      password = "password".some,
      database = "todo",
      max = 10,
      debug = false,
    )
