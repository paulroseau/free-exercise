package exercise.controller

import scala.concurrent.{ Future, ExecutionContext }

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{ Route, RouteResult }

import argonaut._, Argonaut._, ArgonautShapeless._

import exercise.model.User
import exercise.db.UserRepository
import exercise.util.ResponseMessage

class RepoController(repo: UserRepository) {

  import RepoController._

  def getUserId(
    uid: Long
  ): Route = {
      val userOpt = repo.getUser(uid)
      userOpt match {
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
    val uid = repo.createUser(user)
    ctx => ctx.complete(userCreated(uid).toHttp(StatusCodes.OK))
  }

  def updateUser(
    uid: Long,
    newUser: User
  ): Route = 
    repo.updateUser(uid, newUser) match {
      case Some(_) => 
        ctx => ctx.complete(userUpdated(uid).toHttp(StatusCodes.OK))
      case None => 
        ctx => ctx.complete(userNotFound(uid).toHttp(StatusCodes.NotFound))
    }

  def deleteUser(uid: Long): Route =
    repo.deleteUser(uid) match {
      case Some(_) => 
        ctx => ctx.complete(userDeleted(uid).toHttp(StatusCodes.OK))
      case None => 
        ctx => ctx.complete(userNotFound(uid).toHttp(StatusCodes.NotFound))
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
