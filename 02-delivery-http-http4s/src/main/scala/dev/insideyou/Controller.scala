package dev.insideyou

import org.http4s.HttpRoutes

trait Controller:
  def routes: HttpRoutes[Z]
