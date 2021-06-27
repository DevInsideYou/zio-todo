package dev.insideyou
package todo

import zio.*

object Program:
  lazy val make: Z[Unit] =
    SessionPool.make.use { resource =>
      for
        controller <- crud.DependencyGraph.make(Pattern, resource)
        server <- Server.make {
          HttpApp.make(
            controller
          )
        }
        _ <- server.serve
      yield ()
    }

  private lazy val Pattern =
    DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy HH:mm")
