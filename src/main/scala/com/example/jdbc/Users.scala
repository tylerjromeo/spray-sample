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

  class Users(tag: Tag) extends Table[(Int, String)](tag, "users") {


    def id = column[Int]("user_id", O.PrimaryKey)

    // This is the primary key column
    def name = column[String]("name")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, name)
  }

  val users = TableQuery[Users]

  def main(args: Array[String]) {
    val db = Database.forConfig("postgres")
    try {
      println("Users:")
      db.run(users.result).map(_.foreach {
        case (id, name) =>
          println("  " + name + "\t" + id)
      })
    } finally db.close
  }
}
