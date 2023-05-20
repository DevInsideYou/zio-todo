package dev.insideyou
package todo

import zio.*

trait Random[-R, +E]:
  def nextInt(n: Int): ZIO[R, E, Int]

object Random:
  lazy val make: UIO[Random[Any, Nothing]] =
    ZIO.succeed:
      new:
        override def nextInt(n: Int): UIO[Int] =
          ZIO.succeed(scala.util.Random.nextInt(n))
