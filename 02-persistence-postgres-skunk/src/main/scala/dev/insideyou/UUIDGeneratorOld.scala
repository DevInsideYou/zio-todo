package dev.insideyou

import cats.*

trait UUIDGeneratorOld[F[_]]:
  def genUUID: F[UUID]

object UUIDGeneratorOld:
  def make[F[_]](using S: effect.Sync[F]): F[UUIDGeneratorOld[F]] =
    S.delay {
      new:
        override lazy val genUUID: F[UUID] =
          S.delay(UUID.randomUUID())
    }
