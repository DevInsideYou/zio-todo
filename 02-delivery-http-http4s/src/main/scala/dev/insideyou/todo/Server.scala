package dev.insideyou
package todo

import org.http4s.*
import org.http4s.ember.server.EmberServerBuilder
import zio.*

trait Server[-R, +E]:
  def serve: ZIO[R, E, Unit]

object Server:
  def make(httpApp: HttpApp[Z]): UIO[Server[ZEnv, Throwable]] =
    ZIO.succeed:
      new:
        override lazy val serve =
          EmberServerBuilder
            .default[Z]
            .withHttpApp(httpApp)
            .build
            .useForever
