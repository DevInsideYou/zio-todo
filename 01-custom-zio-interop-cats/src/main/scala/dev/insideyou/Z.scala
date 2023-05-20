package dev.insideyou

import zio.*

type ZEnv = ZIOAppArgs & Scope
type Z[+A] = RIO[ZEnv, A]
