package dev.insideyou

import zio.*

trait UUIDGenerator[-R, +E]:
  def genUUID: ZIO[R, E, UUID]

object UUIDGenerator:
  lazy val make: UIO[UUIDGenerator[Any, Nothing]] =
    ZIO.succeed:
      new:
        override lazy val genUUID: UIO[UUID] =
          ZIO.succeed(UUID.randomUUID())
