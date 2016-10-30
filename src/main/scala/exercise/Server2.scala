package exercise

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.io.StdIn

import cats.free.Inject
import cats.instances._

import exercise.algebra.{ LogOps, StoreOps, SerializationOps }
import exercise.controller.MainController
import exercise.db.InMemoryUserRepoSync
import exercise.interpreter.{ LoggerInterpreter, StoreInterpreter, SerializerInterpreter }
import exercise.util.ToFutureConv

object Server2 extends FutureInstances {

  import ToFutureConv._

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("main-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    import Inject._ // implicits for Coproducts here
    implicit val storeOps = 
      new StoreOps[MainController.Op]
    implicit val logOps = 
      new LogOps[MainController.Op]
    implicit val serializationOps = 
      new SerializationOps[MainController.Op]

    implicit val storeInterpreter = 
      StoreInterpreter.futureInterpreter(new InMemoryUserRepoSync)
    implicit val loggerInterpreter = 
      LoggerInterpreter.futureInterpreter
    implicit val serializerInterpreter = 
      SerializerInterpreter.futureInterpreter

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
