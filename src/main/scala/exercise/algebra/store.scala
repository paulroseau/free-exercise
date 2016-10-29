package exercise.algebra

import exercise.model.User

import cats.free.Free

sealed trait StoreOp[T]
case class CreateUser(user: User) extends StoreOp[Long]
case class GetUser(id: Long) extends StoreOp[Option[User]]
case class UpdateUser(id: Long, newUser: User) extends StoreOp[Option[Unit]]
case class DeleteUser(id: Long) extends StoreOp[Option[Unit]]

object StoreOps {

  def createUser(user: User): Free[StoreOp, Long] =
    Free.liftF(CreateUser(user))

  def getUser(id: Long): Free[StoreOp, Option[User]] =
    Free.liftF(GetUser(id))

  def updateUser(id: Long, newUser: User): Free[StoreOp, Option[Unit]] =
    Free.liftF(UpdateUser(id, newUser))

  def deleteUser(id: Long): Free[StoreOp, Option[Unit]] =
    Free.liftF(DeleteUser(id))
}
