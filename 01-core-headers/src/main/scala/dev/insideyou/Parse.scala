package dev.insideyou

trait Parse[-From, +To] extends Function1[From, Either[Throwable, To]]
