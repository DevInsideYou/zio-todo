package dev.insideyou
package todo
package crud

import skunk.*
import skunk.codec.all.*
import skunk.implicits.*

object Statement:
  extension (todo: insert.Todo.type)
    private def codec: Codec[insert.Todo] =
      (text ~ timestamp).gimap[insert.Todo]

  extension (existing: Todo.type)
    def codec: Codec[Todo[UUID]] =
      (uuid ~ text ~ timestamp).gimap[Todo[UUID]]

  object Insert:
    val one: Query[insert.Todo, Todo[UUID]] =
      sql"""
               INSERT INTO todo (description, deadline)
               VALUES (${insert.Todo.codec})
            RETURNING *
         """.query(Todo.codec)

    def many(size: Int): Query[List[insert.Todo], Todo[UUID]] =
      sql"""
               INSERT INTO todo (description, deadline)
               VALUES (${insert.Todo.codec.list(size)})
            RETURNING *
         """.query(Todo.codec)

    object WithUUID:
      val one: Command[Todo[UUID]] =
        sql"""
              INSERT INTO todo
              VALUES (${Todo.codec})
           """.command

      def many(size: Int): Command[List[Todo[UUID]]] =
        sql"""
              INSERT INTO todo
              VALUES (${Todo.codec.list(size)})
           """.command

  object Update:
    val one: Query[Todo[UUID], Todo[UUID]] =
      sql"""
               UPDATE todo
                  SET description = $text, deadline = $timestamp
                WHERE id = $uuid
            RETURNING *
         """.query(Todo.codec).contramap(toTwiddle)

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
         """.query(Todo.codec)

    def many(size: Int): Query[List[UUID], Todo[UUID]] =
      sql"""
            SELECT *
              FROM todo
             WHERE id IN (${uuid.list(size)})
         """.query(Todo.codec)

    val byDescription: Query[String, Todo[UUID]] =
      sql"""
            SELECT *
              FROM todo
             WHERE description ~ $text
         """.query(Todo.codec)

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
