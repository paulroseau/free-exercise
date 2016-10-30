package exercise

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.io.StdIn

import exercise.controller.MainController
import exercise.db.InMemoryUserRepoSync
import exercise.interpreter.{ LoggerInterpreter, StoreInterpreter }
import exercise.util.ToFutureConv

object Server2 {

  import ToFutureConv._

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("main-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    implicit val storeInterpreter = 
      StoreInterpreter.futureInterpreter(new InMemoryUserRepoSync)
    implicit val loggerInterpreter = 
      LoggerInterpreter.futureInterpreter

    val controller = new MainController[Future]()

    val route: Route = 
      pathPrefix("user") {
        pathSuffix(LongNumber) { uid =>
          get { 
            ctx => controller.getUser(uid)
          } ~
          put { 
            ctx => controller.updateUser(uid, ctx.request.entity)
          } ~
          delete { 
            ctx => controller.deleteUser(uid)
          } 
        } ~
        post { 
          ctx => controller.createUser(ctx.request.entity)
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
