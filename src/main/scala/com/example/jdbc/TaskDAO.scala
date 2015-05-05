package com.example.jdbc

/**
 * User: tylerromeo
 * Date: 5/5/15
 * Time: 2:25 PM
 *
 */

import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.PostgresDriver.api._
import spray.json._
import DefaultJsonProtocol._

import scala.concurrent.Future
import scala.util.{Success, Failure}

object TaskDAO {
  //TODO unit tests

  val db = Database.forConfig("postgres")

  case class Task(
                   id: Option[Int],
                   text: String,
                   complete: Boolean
                   )

  object Task {
    implicit val taskFormat = jsonFormat3(Task.apply)
  }

  class Tasks(tag: Tag) extends Table[Task](tag, "tasks") {
    def id = column[Int]("task_id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("text")

    def complete = column[Boolean]("complete")

    def * = (id.?, name, complete) <>((Task.apply _).tupled, Task.unapply)
  }

  val tasksQuery = TableQuery[Tasks]

  val dropTables = tasksQuery.schema.drop

  val createTables = tasksQuery.schema.create

  val setup =
    tasksQuery.schema.drop.asTry andThen DBIO.seq(
      createTables,
      addTask(Task(None, "Do the first thing", complete = true)),
      addTask(Task(None, "Do the second thing", complete = false)),
      addTask(Task(None, "Do the third thing", complete = false))
    )

  def addTask(task: Task) = tasksQuery += task

  def getTasks = tasksQuery.result

  def getTaskById(taskId: Int) = tasksQuery.filter(_.id === taskId).result.headOption

  def deleteTask(taskId: Int) = tasksQuery.filter(_.id === taskId).delete

  def updateTask(task: Task) = tasksQuery.filter(_.id === task.id).update(task)

  def main(args: Array[String]) {
    try {
      //      db.run(setup) onComplete {
      //        case Success(x) => {
      //          println("Success!!!")
      //        }
      //        case Failure(t) => {
      //          println("Failure")
      //          t.printStackTrace
      //        }
      //      }
      println(Task(Some(1), "test", true).toJson.prettyPrint)
      db.run(getTasks) onComplete {
        case Success(x) => {
          println("Success!!!")
          x.map(task => {
            println(s"${task.id} ${task.text} ${task.complete}")
            println(task.toJson.prettyPrint)
          })
        }
        case Failure(t) => {
          println("Failure")
          t.printStackTrace
        }
      }
      //      println(Task(Some(1), "test", true).toJson.prettyPrint)
      //            db.run(getTaskById(3)) onComplete {
      //              case Success(x) => {
      //                println("Success!!!")
      //                println(x.toJson.prettyPrint)
      //                x match {
      //                  case None => println("no task for that id")
      //                  case Some(task) => println(Task(Some(1), "test", true).toJson.prettyPrint) /*println(s"${task.id} ${task.text} ${task.complete}")*/
      //                }
      //              }
      //              case Failure(t) => {
      //                println("Failure")
      //                t.printStackTrace
      //              }
      //            }
      //      db.run(deleteTask(1)) onComplete {
      //        case Success(x) => {
      //          println("Success!!!")
      //        }
      //        case Failure(t) => {
      //          println("Failure")
      //          t.printStackTrace
      //        }
      //      }
      //      db.run(updateTask(Task(Some(2), "blorgle", true))) onComplete {
      //        case Success(x) => {
      //          println("Success!!!")
      //        }
      //        case Failure(t) => {
      //          println("Failure")
      //          t.printStackTrace
      //        }
      //      }
    } finally db.close()
  }

}
