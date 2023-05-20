package dev.insideyou
package todo

import zio.*

object Main extends ZIOAppDefault:
  override lazy val run =
    Program.make
