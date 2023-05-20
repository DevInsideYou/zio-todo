package dev.insideyou
package todo

object Program:
  lazy val make: Z[Unit] =
    for
      controller <- crud.DependencyGraph.make(Pattern)
      server <- Server.make(HttpApp.make(controller))
      _ <- server.serve
    yield ()

  private lazy val Pattern =
    DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy HH:mm")
