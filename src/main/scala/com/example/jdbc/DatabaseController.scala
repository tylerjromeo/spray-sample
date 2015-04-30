package com.example.jdbc

/**
 * User: tylerromeo
 * Date: 4/30/15
 * Time: 12:48 PM
 *
 */


import slick.driver.PostgresDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

object DatabaseController {

  lazy val db = Database.forConfig("postgres")

  type User = (Int, String)

  class Users(tag: Tag) extends Table[(Int, String)](tag, "users") {

    def id = column[Int]("user_id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def * = (id, name)
  }

  lazy val users = TableQuery[Users]

  type List = (Int, String, Int)

  class Lists(tag: Tag) extends Table[(Int, String, Int)](tag, "lists") {

    def id = column[Int]("list_id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def userId = column[Int]("user_id")

    def * = (id, name, userId)

    def user = foreignKey("users", userId, users)(_.id)
  }

  lazy val lists = TableQuery[Lists]

  def getAllUserNames = {
    try {
      val query = users.map(_.name)
      db.run(query.result)
    } finally db.close
  }

  def getAllListNames = {
    try {
      val query = lists.map(_.name)
      db.run(query.result)
    } finally db.close
  }

  def getUsersLists(userId:Int) = {
    try {
      db.run(lists.filter(_.userId === userId).map(_.name).result)
    } finally db.close
  }

  def main(args: Array[String]) {
//    getAllUserNames onComplete {
//      case Success(x) => {
//        println("Users:")
//        x.foreach(println)
//      }
//      case Failure(t) => println("Could not get Users: " + t.getMessage)
//    }

//    getAllListNames onComplete {
//      case Success(x) => {
//        println("Lists:")
//        x.foreach(println)
//      }
//      case Failure(t) => println("Could not get Lists: " + t.getMessage)
//    }

    getUsersLists(1) onComplete {
      case Success(x) => {
        println("Lists for user 1:")
        x.foreach(println)
      }
      case Failure(t) => println("Could not get Lists: " + t.getMessage)
    }
  }
}
