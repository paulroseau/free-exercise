package exercise.controller

import scala.language.{ higherKinds, implicitConversions }
import scala.concurrent.Future

import akka.http.scaladsl.server.RouteResult
import akka.http.scaladsl.model.{ HttpEntity, HttpRequest }

import cats.arrow.FunctionK
import cats.data.Coproduct

import exercise.algebra._
import exercise.interpreter.Interpreter
import exercise.util.ToFutureConv

class MainController[F[_]](implicit 
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

  def getUser(uid: Long): F[RouteResult] = ???

  def updateUser(uid: Long, entity: HttpEntity): F[RouteResult] = ???

  def deleteUser(uid: Long): F[RouteResult] = ???

}

object MainController {

  type Op0[T] = Coproduct[LogOp, SerializationOp, T]
  type Op[T] = Coproduct[StoreOp, Op0, T]

}
