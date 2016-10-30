package exercise.algebra

import scala.language.higherKinds

import exercise.model.User

import cats.free.{ Free, Inject }

sealed trait StoreOp[T]
case class CreateUser(user: User) extends StoreOp[Long]
case class GetUser(id: Long) extends StoreOp[Option[User]]
case class UpdateUser(id: Long, newUser: User) extends StoreOp[Option[Unit]]
case class DeleteUser(id: Long) extends StoreOp[Option[Unit]]

class StoreOps[F[_]](implicit ev: Inject[StoreOp, F])  {

  def createUser(user: User): Free[F, Long] =
    Free.inject(CreateUser(user))

  def getUser(id: Long): Free[F, Option[User]] =
    Free.inject(GetUser(id))

  def updateUser(id: Long, newUser: User): Free[F, Option[Unit]] =
    Free.inject(UpdateUser(id, newUser))

  def deleteUser(id: Long): Free[F, Option[Unit]] =
    Free.inject(DeleteUser(id))
}
