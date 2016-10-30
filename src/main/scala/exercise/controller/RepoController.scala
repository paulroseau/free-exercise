package exercise.controller

import scala.concurrent.{ Future, ExecutionContext }

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{ Route, RouteResult }

import argonaut._, Argonaut._, ArgonautShapeless._

import cats.data.Coproduct

import exercise.algebra._
import exercise.db.UserRepository
import exercise.interpreter.{ StoreInterpreter, StoreLoggingInterpreter }
import exercise.model.User
import exercise.util.ResponseMessage

class RepoController(repo: UserRepository)(implicit
  storeOps: StoreOps[Coproduct[StoreOp, StoreLoggingOp, ?]],
  logOps: StoreLoggingOps[Coproduct[StoreOp, StoreLoggingOp, ?]]
) {

  import RepoController._

  val interpreter = 
    StoreInterpreter.syncImpure(repo) 
      .or(StoreLoggingInterpreter.idInterpreter)

  def getUser(uid: Long): Route = {

    val action = for {
      userOpt <- storeOps.getUser(uid)
      _ <- logOps.logUserRetrieval(uid, userOpt)
    } yield userOpt

    action.foldMap(interpreter) match {
      case Some(user) =>
        ctx => ctx.complete(
          HttpResponse(
            status = StatusCodes.OK, 
            entity = HttpEntity(
              ContentTypes.`application/json`,
              user.asJson.spaces2)))
      case None =>
        ctx => ctx.complete(userNotFound(uid).toHttp(StatusCodes.NotFound))
    } 
  }

  def createUser(
    user: User
  ): Route = {

    val action = for {
      uid <- storeOps.createUser(user)
      _ <- logOps.logUserCreation(uid, user)
    } yield uid

    ctx => ctx.complete(
      userCreated(action.foldMap(interpreter))
        .toHttp(StatusCodes.OK)
      )
  }

  def updateUser(
    uid: Long,
    newUser: User
  ): Route = {

    val action = for {
      opt <- storeOps.updateUser(uid, newUser)
      _ <- logOps.logUserUpdate(uid, opt, newUser)
    } yield opt

    action.foldMap(interpreter) match {
      case Some(_) => 
        ctx => ctx.complete(userUpdated(uid).toHttp(StatusCodes.OK))
      case None => 
        ctx => ctx.complete(userNotFound(uid).toHttp(StatusCodes.NotFound))
    }
  }

  def deleteUser(uid: Long): Route = {

    val action = for {
      opt <- storeOps.deleteUser(uid)
      _ <- logOps.logUserDeletion(uid, opt)
    } yield opt

    action.foldMap(interpreter) match {
      case Some(_) => 
        ctx => ctx.complete(userDeleted(uid).toHttp(StatusCodes.OK))
      case None => 
        ctx => ctx.complete(userNotFound(uid).toHttp(StatusCodes.NotFound))
    }
  }
}

object RepoController {

  def apply(
    repo: UserRepository
  )(implicit
    storeOps: StoreOps[({type X[T] = Coproduct[StoreOp, StoreLoggingOp, T]})#X],
    logOps: StoreLoggingOps[({type X[T] = Coproduct[StoreOp, StoreLoggingOp, T]})#X]
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
