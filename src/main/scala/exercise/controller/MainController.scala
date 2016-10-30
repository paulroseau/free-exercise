package exercise.controller

import scala.language.{ higherKinds, implicitConversions }
import scala.concurrent.Future

import akka.http.scaladsl.server.RouteResult
import akka.http.scaladsl.model.{ HttpEntity, HttpRequest }

import cats.arrow.FunctionK

import exercise.algebra.StoreOp
import exercise.interpreter.Interpreter
import exercise.util.ToFutureConv

class MainController[F[_]](implicit 
  converter: ToFutureConv[F],
  storeConverter: FunctionK[StoreOp, F]
) {

  implicit def ftoFuture[T](f: F[T]): Future[T] = 
    converter(f)

  val interpreter = Interpreter[MainController.Op, F]

  def createUser(entity: HttpEntity): F[RouteResult] = ???

  def getUser(uid: Long): F[RouteResult] = ???

  def updateUser(uid: Long, entity: HttpEntity): F[RouteResult] = ???

  def deleteUser(uid: Long): F[RouteResult] = ???

}

object MainController {

  type Op[T] = StoreOp[T]

}
