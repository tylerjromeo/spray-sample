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
    (users.schema ++ lists.schema ++ listItems.schema).drop,//TODO don't fail if the tables don't exist
    // Create the tables, including primary and foreign keys
    (users.schema ++ lists.schema ++ listItems.schema).create
  )

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

  type ListItem = (Int, String, Boolean, Int)

  class ListItems(tag: Tag) extends Table[(Int, String, Boolean, Int)](tag, "list_items") {

    def id = column[Int]("list_item_id", O.PrimaryKey, O.AutoInc)

    def text = column[String]("text")

    def complete = column[Boolean]("complete")

    def listId = column[Int]("list_id")

    def * = (id, text, complete, listId)

    def list = foreignKey("lists", listId, lists)(_.id)
  }

  lazy val listItems = TableQuery[ListItems]

  def getAllUsers = {
    try {
      val query = users
      db.run(query.result)
    } finally db.close()
  }

  def getUser(userId: Int) = {
    try {
      val query = users.filter(_.id === userId)
      db.run(query.result)
    } finally db.close()
  }

  def getAllLists = {
    try {
      val query = lists
      db.run(query.result)
    } finally db.close()
  }

  def getUsersLists(userId: Int) = {
    try {
      val query = lists.filter(_.userId === userId).result
        db.run(query)
    } finally db.close()
  }

  def getList(listId: Int) = {
    try {
      val query = lists.filter(_.id === listId).result
      db.run(query)
    } finally db.close()
  }

  def getListsItems(listId: Int) = {
    try {
      val query = listItems.filter(_.listId === listId).result
      db.run(query)
    } finally db.close()
  }

  def main(args: Array[String]) {
    //TODO finish testing bulk import
    println("test")
    db.run(recreateTables) onComplete {
      case Success(x) => {
        println("Success!!!")
      }
      case Failure(t) => {
        println("Failure")
        t.printStackTrace
      }
    }
  }
}
