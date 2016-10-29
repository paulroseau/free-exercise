package exercise.controller

import scala.concurrent.{ Future, ExecutionContext }

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{ Route, RouteResult }

import argonaut._, Argonaut._, ArgonautShapeless._

import exercise.algebra.StoreOps
import exercise.db.UserRepository
import exercise.interpreter.StoreInterpreter
import exercise.model.User
import exercise.util.ResponseMessage

class RepoController(repo: UserRepository) {

  import RepoController._

  val interpreter = StoreInterpreter.syncImpure(repo)

  def getUserId(uid: Long): Route = {

    val action = for {
      userOpt <- StoreOps.getUser(uid)
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
      uid <- StoreOps.createUser(user)
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
      opt <- StoreOps.updateUser(uid, newUser)
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
      opt <- StoreOps.deleteUser(uid)
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

  def apply(repo: UserRepository): RepoController =
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
