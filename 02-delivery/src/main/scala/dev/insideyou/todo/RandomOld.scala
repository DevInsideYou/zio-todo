package dev.insideyou
package todo

import cats.*

trait RandomOld[F[_]]:
  def nextInt(n: Int): F[Int]

object RandomOld:
  def make[F[_]](using S: effect.Sync[F]): F[RandomOld[F]] =
    S.delay {
      new:
        override def nextInt(n: Int): F[Int] =
          S.delay(scala.util.Random.nextInt(n))
    }
