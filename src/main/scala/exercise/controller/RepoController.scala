package exercise.controller

import scala.concurrent.{ Future, ExecutionContext }

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{ Route, RouteResult }

import exercise.model.User
import exercise.db.UserRepository
import exercise.util.ResponseMessage

import argonaut._, Argonaut._, ArgonautShapeless._

class RepoController(
  repo: UserRepository
) {

  import RepoController._

  def getUserId(
    uid: Long
  ): Route = ctx => {
      val userOpt = repo.getUser(uid)
      userOpt match {
        case Some(user) =>
          ctx.complete(
            HttpResponse(
              status = StatusCodes.OK, 
              entity = HttpEntity(
                ContentTypes.`application/json`,
                user.asJson.spaces2)))
        case None =>
          ctx.complete(StatusCodes.NotFound, userNotFound(uid).asJson.spaces2)
      } 
    }

  def createUser(
    user: User
  ): Route = ctx => {
    val uid = repo.createUser(user)
    ctx.complete(response(StatusCodes.OK, userCreated(uid)))
  }

  def updateUser(
    uid: Long,
    newUser: User
  ): Route = ctx => {
    repo.updateUser(uid, newUser) match {
      case Some(_) => ctx.complete(response(StatusCodes.OK, userUpdated(uid)))
      case None => ctx.complete(StatusCodes.NotFound, userNotFound(uid).asJson.spaces2)
    }
  }

  def deleteUser(uid: Long): Route = ctx => {
    repo.deleteUser(uid) match {
      case Some(_) => ctx.complete(response(StatusCodes.OK, userDeleted(uid)))
      case None => ctx.complete(StatusCodes.NotFound, userNotFound(uid).asJson.spaces2)
    }
  }

}

object RepoController {

  def apply(repo: UserRepository): RepoController =
    new RepoController(repo)

  def response(
    status: StatusCode,
    msg: ResponseMessage
  ): HttpResponse =
    HttpResponse(
      status = status, 
      entity = HttpEntity(
        ContentTypes.`application/json`,
        msg.asJson.spaces2))

  def userCreated(uid: Long) =
    ResponseMessage("success", s"User id $uid created")

  def userUpdated(uid: Long) =
    ResponseMessage("success", s"User id $uid updated")

  def userDeleted(uid: Long) =
    ResponseMessage("success", s"User id $uid deleted")

  def userNotFound(uid: Long) =
    ResponseMessage("error", s"User id $uid does not reference any user")
}
