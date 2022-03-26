package dev.insideyou
package todo
package crud
package insert

import zio.*

final class BoundarySuite extends TestSuite:
  import BoundarySuite.*

  test("description should be trimmed") {
    val boundary =
      makeBoundary {
        new:
          override def createMany(todos: Vector[Todo]): UIO[Vector[crud.Todo[Unit]]] =
            ZIO.succeed {
              todos.map { todo =>
                crud.Todo((), todo.description, todo.deadline)
              }
            }
      }

    forAll { (insertTodoG: Todo) =>
      val insertTodo =
        insertTodoG.withUpdatedDescription(s"  ${insertTodoG.description}  ")

      Runtime.default.unsafeRun {
        boundary.createOne(insertTodo).map { todo =>
          todo.description `shouldBe` insertTodo.description.trim
        }
      }
    }
  }

  private def makeBoundary[TodoId](gate: FakeGate[TodoId]): Boundary[Any, Throwable, TodoId] =
    BoundaryImpl.make(gate)

object BoundarySuite:
  private class FakeGate[TodoId] extends Gate[Any, Nothing, TodoId]:
    override def createMany(todos: Vector[Todo]): UIO[Vector[crud.Todo[TodoId]]] = ???
