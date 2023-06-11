package dev.insideyou
package todo

import com.comcast.ip4s.*
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
            .withPort(sys.env.get("HTTP_PORT").flatMap(Port.fromString).getOrElse(port"8080"))
            .build
            .useForever
