package com.example.jdbc

/**
 * User: tylerromeo
 * Date: 4/30/15
 * Time: 12:48 PM
 *
 */


import slick.driver.PostgresDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global

object DatabaseController {

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

//    def user = foreignKey("users", userId, users)
  }

  lazy val lists = TableQuery[Users]

  def getAllUserNames = {
    val db = Database.forConfig("postgres")
    try {
      val query = users.map(_.name)
      db.run(query.result)
    } finally db.close
  }

  def main(args: Array[String]) {
    println("Users:")
    getAllUserNames onSuccess {
      case x => {
        x.foreach(println)
      }
    }
  }
}
