package dev.insideyou
package todo
package crud

import skunk.*
import skunk.codec.all.*
import skunk.implicits.*
import org.typelevel.twiddles.syntax.*

object Statement:
  extension (data: Todo.Data.type)
    def codec: Codec[Todo.Data] =
      (text *: timestamp).to[Todo.Data]

  extension (existing: Todo.Existing.type)
    def codec: Codec[Todo.Existing[UUID]] =
      (uuid *: Todo.Data.codec).to[Todo.Existing[UUID]]

  object Insert:
    val one: Query[Todo.Data, Todo.Existing[UUID]] =
      sql"""
               INSERT INTO todo (description, deadline)
               VALUES (${Todo.Data.codec})
            RETURNING *
         """.query(Todo.Existing.codec)

    def many(size: Int): Query[List[Todo.Data], Todo.Existing[UUID]] =
      sql"""
               INSERT INTO todo (description, deadline)
               VALUES (${Todo.Data.codec.list(size)})
            RETURNING *
         """.query(Todo.Existing.codec)

    object WithUUID:
      val one: Command[Todo.Existing[UUID]] =
        sql"""
              INSERT INTO todo
              VALUES (${Todo.Existing.codec})
           """.command

      def many(size: Int): Command[List[Todo.Existing[UUID]]] =
        sql"""
              INSERT INTO todo
              VALUES (${Todo.Existing.codec.list(size)})
           """.command

  object Update:
    val one: Query[Todo.Existing[UUID], Todo.Existing[UUID]] =
      sql"""
               UPDATE todo
                  SET description = $text, deadline = $timestamp
                WHERE id = $uuid
            RETURNING *
         """.query(Todo.Existing.codec).contramap(toTwiddle)

    object Command:
      val one: Command[Todo.Existing[UUID]] =
        sql"""
              UPDATE todo
                 SET description = $text, deadline = $timestamp
               WHERE id = $uuid
           """.command.contramap(toTwiddle)

    private def toTwiddle(e: Todo.Existing[UUID]): (String, LocalDateTime, UUID) =
      (e.data.description, e.data.deadline, e.id)

  object Select:
    val all: Query[Void, Todo.Existing[UUID]] =
      sql"""
            SELECT *
              FROM todo
         """.query(Todo.Existing.codec)

    def many(size: Int): Query[List[UUID], Todo.Existing[UUID]] =
      sql"""
            SELECT *
              FROM todo
             WHERE id IN (${uuid.list(size)})
         """.query(Todo.Existing.codec)

    val byDescription: Query[String, Todo.Existing[UUID]] =
      sql"""
            SELECT *
              FROM todo
             WHERE description ~ $text
         """.query(Todo.Existing.codec)

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
