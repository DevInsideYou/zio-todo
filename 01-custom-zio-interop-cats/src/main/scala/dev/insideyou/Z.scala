package dev.insideyou

import zio.*

type Z[+A] = RIO[ZEnv, A]
