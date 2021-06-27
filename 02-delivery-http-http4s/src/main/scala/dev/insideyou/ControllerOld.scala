package dev.insideyou

import org.http4s.HttpRoutes

trait ControllerOld[F[_]]:
  def routes: HttpRoutes[F]
