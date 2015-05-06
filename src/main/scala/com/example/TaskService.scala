package com.example

import akka.actor.Actor
import com.example.jdbc.TaskDAO
import com.example.jdbc.TaskDAO.Task
import spray.http.HttpHeaders.Location
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
    pathPrefix("tasks") {
      pathEndOrSingleSlash {
        get {
          respondWithMediaType(`application/json`) {
            onComplete(TaskDAO.getTasks) {
              case Success(value) => complete(value)
              case Failure(ex) => complete(s"An error occurred: ${ex.getMessage}")
            }
          }
        } ~
          post {
            entity(as[Task]) { task =>
              onComplete(TaskDAO.addTask(task)) {
                case Success(value) =>
                  requestUri { uri =>
                    respondWithHeader(Location(uri + s"/${value}")) {
                      complete(StatusCodes.Created)
                    }
                  }
                case Failure(ex) => complete(s"An error occurred: ${ex.getMessage}")
              }
            }
          }
      } ~
        path(IntNumber) { param =>
          get {
            respondWithMediaType(`application/json`) {
              onComplete(TaskDAO.getTaskById(param)) {
                case Success(value) => complete(value)
                case Failure(ex) => complete(s"An error occurred: ${ex.getMessage}")
              }
            }
          } ~
            delete {
              onComplete(TaskDAO.deleteTask(param)) {
                case Success(value) => complete(StatusCodes.NoContent)
                case Failure(ex) => complete(s"An error occurred: ${ex.getMessage}")
              }
            } ~
            put {
              entity(as[Task]) { task =>
                //if the task doesn't have an id, fail. If it does make sure it matches the path param
                validate(task.id.exists(_ == param), "Task id does not match path") {
                  onComplete(TaskDAO.updateTask(task)) {
                    case Success(value) =>
                      requestUri { uri =>
                        respondWithHeader(Location(uri)) {
                          complete(StatusCodes.Created)
                        }
                      }
                    case Failure(ex) => complete(s"An error occurred: ${ex.getMessage}")
                  }
                }
              }
            }
        }
    }
}
