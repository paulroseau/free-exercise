package exercise.interpreter

import cats.Id
import cats.arrow.FunctionK

import exercise.algebra._
import exercise.db.UserRepository

object StoreInterpreter {

  def syncImpure(repo: UserRepository): FunctionK[StoreOp, Id] =
    new FunctionK[StoreOp, Id] {

      def apply[T](storeOp: StoreOp[T]): Id[T] =
        storeOp match {
          case CreateUser(user) => repo.createUser(user)
          case GetUser(uid) => repo.getUser(uid)
          case UpdateUser(uid, newUser) => repo.updateUser(uid, newUser)
          case DeleteUser(uid) => repo.deleteUser(uid)
        }
    }
}
