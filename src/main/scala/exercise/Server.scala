package exercise

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.io.StdIn

import cats.data.Coproduct
import cats.free.Inject

import exercise.algebra._
import exercise.controller.RepoController
import exercise.db.InMemoryUserRepo
import exercise.model.User
import exercise.util._

object Server {

  def main(args: Array[String]): Unit = {
 

    implicit val system = ActorSystem("main-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    import Inject._ // implicits for Coproducts here
    implicit val storeOps = new StoreOps[Coproduct[StoreOp, StoreLoggingOp, ?]]
    implicit val logOps = new StoreLoggingOps[Coproduct[StoreOp, StoreLoggingOp, ?]]
    val controller = RepoController(new InMemoryUserRepo)

    import CustomDirectives._

    val route =
      pathPrefix("user") {
        pathSuffix(LongNumber) { uid =>
          get { 
            controller.getUserId(uid)
          } ~
          put { 
            extractWithArgonaut[User](User.decoder) { newUser =>
              controller.updateUser(uid, newUser)
            }
          } ~
          delete { 
            controller.deleteUser(uid)
          } 
        } ~
        post { 
          extractWithArgonaut[User](User.decoder) { user =>
            controller.createUser(user)
          }
        }
      }
 
    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
 
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
