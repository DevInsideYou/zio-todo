package dev.insideyou

import cats.*

trait UUIDGenerator[F[_]]:
  def genUUID: F[UUID]

object UUIDGenerator:
  def make[F[_]](using S: effect.Sync[F]): F[UUIDGenerator[F]] =
    S.delay {
      new:
        override lazy val genUUID: F[UUID] =
          S.delay(UUID.randomUUID())
    }
