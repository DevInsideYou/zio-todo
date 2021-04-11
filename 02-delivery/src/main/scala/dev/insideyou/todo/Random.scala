package dev.insideyou
package todo

import cats.*

trait Random[F[_]]:
  def nextInt(n: Int): F[Int]

object Random:
  def make[F[_]](using S: effect.Sync[F]): F[Random[F]] =
    S.delay {
      new:
        override def nextInt(n: Int): F[Int] =
          S.delay(scala.util.Random.nextInt(n))
    }
