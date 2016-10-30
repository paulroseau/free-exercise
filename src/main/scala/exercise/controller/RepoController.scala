package exercise.controller

import scala.concurrent.{ Future, ExecutionContext }

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{ Route, RouteResult }

import argonaut._, Argonaut._, ArgonautShapeless._

import cats.data.Coproduct
import cats.instances.FutureInstances // implicit for making Future a Monad here

import exercise.algebra._
import exercise.db.UserRepository
import exercise.interpreter.{ StoreInterpreter, StoreLoggingInterpreter }
import exercise.model.User
import exercise.util.ResponseMessage

class RepoController(repo: UserRepository)(implicit
  storeOps: StoreOps[Coproduct[StoreOp, StoreLoggingOp, ?]],
  logOps: StoreLoggingOps[Coproduct[StoreOp, StoreLoggingOp, ?]],
  ec: ExecutionContext
) extends FutureInstances {

  import RepoController._

  val interpreter = 
    StoreInterpreter.futureInterpreter(repo) 
      .or(StoreLoggingInterpreter.futureInterpreter)

  def getUser(uid: Long): Route = ctx => {

    val action = for {
      userOpt <- storeOps.getUser(uid)
      _ <- logOps.logUserRetrieval(uid, userOpt)
    } yield userOpt

    action
      .foldMap(interpreter)
      .map {
        case Some(user) =>
          RouteResult.Complete(
            HttpResponse(
            status = StatusCodes.OK, 
            entity = HttpEntity(
              ContentTypes.`application/json`,
              user.asJson.spaces2)))
        case None =>
          RouteResult.Complete(
            userNotFound(uid).toHttp(StatusCodes.NotFound)
          )
    } 
  }

  def createUser(
    user: User
  ): Route = ctx => {

    val action = for {
      uid <- storeOps.createUser(user)
      _ <- logOps.logUserCreation(uid, user)
    } yield uid

    action
      .foldMap(interpreter)
      .map { uid =>
        RouteResult.Complete(
          userCreated(uid).toHttp(StatusCodes.OK)
        )
      }
  }

  def updateUser(
    uid: Long,
    newUser: User
  ): Route = ctx => {

    val action = for {
      opt <- storeOps.updateUser(uid, newUser)
      _ <- logOps.logUserUpdate(uid, opt, newUser)
    } yield opt

    action
      .foldMap(interpreter)
      .map {
        case Some(_) => 
          RouteResult.Complete(
            userUpdated(uid).toHttp(StatusCodes.OK)
          )
        case None => 
          RouteResult.Complete(
            userNotFound(uid).toHttp(StatusCodes.NotFound)
          )
      }
  }

  def deleteUser(uid: Long): Route = ctx => {

    val action = for {
      opt <- storeOps.deleteUser(uid)
      _ <- logOps.logUserDeletion(uid, opt)
    } yield opt

    action
      .foldMap(interpreter) 
      .map {
        case Some(_) => 
          RouteResult.Complete(
            userDeleted(uid).toHttp(StatusCodes.OK)
          )
        case None => 
          RouteResult.Complete(
            userNotFound(uid).toHttp(StatusCodes.NotFound)
          )
      }
  }

}

object RepoController {

  def apply(
    repo: UserRepository
  )(implicit
    storeOps: StoreOps[Coproduct[StoreOp, StoreLoggingOp, ?]],
    logOps: StoreLoggingOps[Coproduct[StoreOp, StoreLoggingOp, ?]],
    ec: ExecutionContext
  ): RepoController =
    new RepoController(repo)

  def userCreated(uid: Long) =
    ResponseMessage("success", s"User id $uid created")

  def userUpdated(uid: Long) =
    ResponseMessage("success", s"User id $uid updated")

  def userDeleted(uid: Long) =
    ResponseMessage("success", s"User id $uid deleted")

  def userNotFound(uid: Long) =
    ResponseMessage("error", s"User id $uid does not reference any user")
}
