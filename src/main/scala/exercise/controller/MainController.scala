package exercise.controller

import scala.language.{ higherKinds, implicitConversions }
import scala.concurrent.Future

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.RouteResult
import akka.http.scaladsl.model.{ HttpEntity, HttpRequest }

import cats.Monad
import cats.arrow.FunctionK
import cats.data.Coproduct
import cats.free.Free

import exercise.algebra._
import exercise.interpreter.Interpreter
import exercise.model.User
import exercise.util.{ ResponseMessage, ToFutureConv }

class MainController[F[_]](implicit 
  MF: Monad[F],
  converter: ToFutureConv[F],
  storeOps: StoreOps[MainController.Op],
  logOps: LogOps[MainController.Op],
  serializationOps: SerializationOps[MainController.Op],
  storeInterpreter: FunctionK[StoreOp, F],
  loggerInterpreter: FunctionK[LogOp, F],
  serializationInterpreter: FunctionK[SerializationOp, F]
) {

  import Interpreter._
  import MainController._

  implicit def ftoFuture[T](f: F[T]): Future[T] = 
    converter(f)

  val interpreter = Interpreter[Op, F]

  def createUser(entity: HttpEntity): F[RouteResult] = ???

  def getUser(uid: Long): F[RouteResult] = {
    val res = 
      Action.getUser(uid).foldMap(interpreter)

    MF.flatMap(res) { 
      case Some(user) => {
        val res0 = 
          Action.serializeUser(user).foldMap(interpreter)

        MF.map(res0) { jsonStr => 
          RouteResult.Complete(
            HttpResponse(
              status = StatusCodes.OK, 
              entity = HttpEntity(
                ContentTypes.`application/json`,
                jsonStr)))
        }
      }
      case None =>
        MF.pure(
          RouteResult.Complete(
            userNotFound(uid).toHttp(StatusCodes.NotFound)))
    }
  }

  def updateUser(uid: Long, entity: HttpEntity): F[RouteResult] = ???

  def deleteUser(uid: Long): F[RouteResult] = {
    val res = 
      Action.deleteUser(uid).foldMap(interpreter)

    MF.flatMap(res) { 
      case Some(_) =>
        MF.pure(
          RouteResult.Complete(
            userUpdated(uid).toHttp(StatusCodes.OK)))
      case None =>
        MF.pure(
          RouteResult.Complete(
            userNotFound(uid).toHttp(StatusCodes.NotFound)))
    }

  }

  object Action {

    def getUser(uid: Long): Free[Op, Option[User]] = for {
      userOpt <- storeOps.getUser(uid)
      _ <- logOps.logUserRetrieval(uid, userOpt)
    } yield userOpt

    def deleteUser(uid: Long): Free[Op, Option[Unit]] = for {
      opt <- storeOps.deleteUser(uid)
      _ <- logOps.logUserDeletion(uid, opt)
    } yield opt

    def serializeUser(user: User): Free[Op, String] = for {
      jsonStr <- serializationOps.serializeUser(user)
    } yield jsonStr
  }
}

object MainController {

  type Op0[T] = Coproduct[LogOp, SerializationOp, T]
  type Op[T] = Coproduct[StoreOp, Op0, T]

  def userCreated(uid: Long) =
    ResponseMessage("success", s"User id $uid created")

  def userUpdated(uid: Long) =
    ResponseMessage("success", s"User id $uid updated")

  def userDeleted(uid: Long) =
    ResponseMessage("success", s"User id $uid deleted")

  def userNotFound(uid: Long) =
    ResponseMessage("error", s"User id $uid does not reference any user")
}
