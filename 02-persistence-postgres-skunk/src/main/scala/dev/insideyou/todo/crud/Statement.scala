package dev.insideyou
package todo
package crud

import skunk.*
import skunk.codec.all.*
import skunk.implicits.*

lazy val codec: Codec[Todo[UUID]] =
  (uuid ~ text ~ timestamp).gimap[Todo[UUID]]

object Statement:
  object Update:
    val one: Query[Todo[UUID], Todo[UUID]] =
      sql"""
               UPDATE todo
                  SET description = $text, deadline = $timestamp
                WHERE id = $uuid
            RETURNING *
         """.query(codec).contramap(toTwiddle)

    object Command:
      val one: Command[Todo[UUID]] =
        sql"""
              UPDATE todo
                 SET description = $text, deadline = $timestamp
               WHERE id = $uuid
           """.command.contramap(toTwiddle)

    private def toTwiddle(e: Todo[UUID]): String ~ LocalDateTime ~ UUID =
      e.description ~ e.deadline ~ e.id

  object Select:
    val all: Query[Void, Todo[UUID]] =
      sql"""
            SELECT *
              FROM todo
         """.query(codec)

    def many(size: Int): Query[List[UUID], Todo[UUID]] =
      sql"""
            SELECT *
              FROM todo
             WHERE id IN (${uuid.list(size)})
         """.query(codec)

    val byDescription: Query[String, Todo[UUID]] =
      sql"""
            SELECT *
              FROM todo
             WHERE description ~ $text
         """.query(codec)

  object Delete:
    val all: Command[Void] =
      sql"""
            DELETE
              FROM todo
         """.command

    def many(size: Int): Command[List[UUID]] =
      sql"""
            DELETE
              FROM todo
             WHERE id IN (${uuid.list(size)})
         """.command
