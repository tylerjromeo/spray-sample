package com.example

import akka.actor.Actor
import com.example.jdbc.TaskDAO
import com.example.jdbc.TaskDAO.Task
import spray.routing._
import spray.http._
import MediaTypes._
import spray.httpx.SprayJsonSupport._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Success, Failure}

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class TaskServiceActor extends Actor with TaskService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(taskRoute)
}


// this trait defines our service behavior independently from the service actor
trait TaskService extends HttpService {

  val taskRoute =
    path("tasks") {
      get {
        respondWithMediaType(`application/json`) {
          // XML is marshalled to `text/xml` by default, so we simply override here
          onComplete(TaskDAO.getTasks) {
            case Success(value) => complete(value)
            case Failure(ex) => complete(s"An error occurred: ${ex.getMessage}")
          }
        }
      }
      post {
        entity(as[Task]) { task =>
          onComplete(TaskDAO.addTask(task)) {
            case Success(value) => complete("")
            case Failure(ex) => complete(s"An error occurred: ${ex.getMessage}")
          }
        }
      }
    } ~
      path("tasks") {
        get {
          respondWithMediaType(`application/json`) {
            // XML is marshalled to `text/xml` by default, so we simply override here
            onComplete(TaskDAO.getTasks) {
              case Success(value) => complete(value)
              case Failure(ex) => complete(s"An error occurred: ${ex.getMessage}")
            }
          }
        }
      }
}
