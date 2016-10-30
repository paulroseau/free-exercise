package exercise.interpreter

import scala.concurrent.{ ExecutionContext, Future }

import cats.Id
import cats.arrow.FunctionK

import exercise.algebra._
import exercise.db.UserRepository

object StoreInterpreter {

  def idInterpreter(
    repo: UserRepository
  ): FunctionK[StoreOp, Id] =
    new FunctionK[StoreOp, Id] {
      def apply[T](storeOp: StoreOp[T]): Id[T] =
        storeOp match {
          case CreateUser(user) => repo.createUser(user)
          case GetUser(uid) => repo.getUser(uid)
          case UpdateUser(uid, newUser) => repo.updateUser(uid, newUser)
          case DeleteUser(uid) => repo.deleteUser(uid)
        }
    }

  def futureInterpreter(
    repo: UserRepository
  )(implicit 
    ec: ExecutionContext
  ): FunctionK[StoreOp, Future] =
    new FunctionK[StoreOp, Future] {
      def apply[T](storeOp: StoreOp[T]): Future[T] =
        storeOp match {
          case CreateUser(user) => Future(repo.createUser(user))
          case GetUser(uid) => Future(repo.getUser(uid))
          case UpdateUser(uid, newUser) => Future(repo.updateUser(uid, newUser))
          case DeleteUser(uid) => Future(repo.deleteUser(uid))
        }
    }

}
