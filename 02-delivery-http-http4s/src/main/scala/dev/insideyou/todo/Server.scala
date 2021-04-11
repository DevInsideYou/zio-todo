package dev.insideyou
package todo

import scala.concurrent.*

import cats.*
import org.http4s.*
import org.http4s.blaze.server.BlazeServerBuilder

trait Server[F[_]]:
  def serve: F[Unit]

object Server:
  def make[F[_]](
      executionContext: ExecutionContext
    )(
      httpApp: HttpApp[F]
    )(using
      A: effect.Async[F]
    ): F[Server[F]] =
    A.delay {
      new:
        override lazy val serve: F[Unit] =
          BlazeServerBuilder(executionContext)
            .bindHttp()
            .withHttpApp(httpApp)
            .serve
            .compile
            .drain
    }
