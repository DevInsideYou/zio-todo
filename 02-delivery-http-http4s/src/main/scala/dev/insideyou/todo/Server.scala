package dev.insideyou
package todo

import scala.concurrent.*

import org.http4s.*
import org.http4s.blaze.server.BlazeServerBuilder
import zio.*

trait Server[-R, +E]:
  def serve: ZIO[R, E, Unit]

object Server:
  def make(httpApp: HttpApp[Z]): UIO[Server[ZEnv, Throwable]] =
    ZIO.succeed {
      new:
        override lazy val serve: Z[Unit] =
          ZIO.runtime.flatMap { runtime =>
            BlazeServerBuilder[Z]
              .withExecutionContext(runtime.platform.executor.asEC)
              .bindHttp()
              .withHttpApp(httpApp)
              .serve
              .compile
              .drain
          }
    }
