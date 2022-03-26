package dev.insideyou
package todo
package crud
package insert

import skunk.*
import skunk.codec.all.*
import skunk.implicits.*

lazy val codec: Codec[Todo] =
  (text ~ timestamp).gimap[Todo]

object Statement:
  object Insert:
    val one: Query[Todo, crud.Todo[UUID]] =
      sql"""
               INSERT INTO todo (description, deadline)
               VALUES (${codec})
            RETURNING *
         """.query(crud.codec)

    def many(size: Int): Query[List[Todo], crud.Todo[UUID]] =
      sql"""
               INSERT INTO todo (description, deadline)
               VALUES (${codec.list(size)})
            RETURNING *
         """.query(crud.codec)

    object WithUUID:
      val one: Command[crud.Todo[UUID]] =
        sql"""
              INSERT INTO todo
              VALUES (${crud.codec})
           """.command

      def many(size: Int): Command[List[crud.Todo[UUID]]] =
        sql"""
              INSERT INTO todo
              VALUES (${crud.codec.list(size)})
           """.command
