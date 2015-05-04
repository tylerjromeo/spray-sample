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

  lazy val recreateTables = DBIO.seq(
    // drop the old tables
    (users.schema ++ lists.schema).drop,
    // Create the tables, including primary and foreign keys
    (users.schema ++ lists.schema).create
  )

  //will return a seq of ids
  lazy val usersData = Seq(
    // Insert some users
    usersReturningId += (0, "Tyler"),
    usersReturningId += (0, "Matt"),
    usersReturningId += (0, "Gregg"),
    usersReturningId += (0, "Kevin")
  )

  def listsData(user: User) = {
    // Insert some lists
    lists += (0, s"${user._2}'s List", user._1)
  }

  type User = (Int, String)

  class Users(tag: Tag) extends Table[(Int, String)](tag, "users") {

    def id = column[Int]("user_id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def * = (id, name)
  }

  lazy val users = TableQuery[Users]
  lazy val usersReturningId = users returning users.map(_.id) into ((user, id) => user.copy(_1=id))

  type List = (Int, String, Int)

  class Lists(tag: Tag) extends Table[(Int, String, Int)](tag, "lists") {

    def id = column[Int]("list_id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def userId = column[Int]("user_id")

    def * = (id, name, userId)

    def user = foreignKey("users", userId, users)(_.id)
  }

  lazy val lists = TableQuery[Lists]

  def recreateDatabase = {
    try {
      db.run(recreateTables)
      usersData.map(data => {
        db.run(data) map {
          user => {
            println(s"test add list for ${user._1}, ${user._2}")
            //TODO: I shouldn't need a whole new database connection here, something must be wrong with the database thread pool config...
            Database.forConfig("postgres").run(listsData(user)) onFailure {
              case t => t.printStackTrace
            }
          }
        }
      })
    } finally db.close
  }


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

  def getUsersLists(userId: Int) = {
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

    recreateDatabase


    //    getUsersLists(1) onComplete {
    //      case Success(x) => {
    //        println("Lists for user 1:")
    //        x.foreach(println)
    //      }
    //      case Failure(t) => println("Could not get Lists: " + t.getMessage)
    //    }
  }
}
