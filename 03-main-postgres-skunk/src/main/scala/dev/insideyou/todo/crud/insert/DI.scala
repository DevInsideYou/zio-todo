package dev.insideyou
package todo
package crud
package insert

import zio.*

def boundary(resource: RManaged[ZEnv, skunk.Session[Z]]): UIO[Boundary[ZEnv, Throwable, UUID]] =
  PostgresGate.make(resource).map(Boundary.make)
