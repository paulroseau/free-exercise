package exercise.algebra

import scala.language.higherKinds

import exercise.model.User

import cats.free.{ Free, Inject }

sealed trait StoreLoggingOp[T]
case class LogUserCreation(uid: Long, user: User) extends StoreLoggingOp[Unit]
case class LogUserRetrieval(uid: Long, userOpt: Option[User]) extends StoreLoggingOp[Unit]
case class LogUserUpdate(uid: Long, opt: Option[Unit], newUser: User) extends StoreLoggingOp[Unit]
case class LogUserDeletion(uid: Long, opt: Option[Unit]) extends StoreLoggingOp[Unit]

class StoreLoggingOps[F[_]](implicit ev: Inject[StoreLoggingOp, F]) {

  def logUserCreation(uid: Long, user: User): Free[F, Unit] =
    Free.inject(LogUserCreation(uid, user))

  def logUserRetrieval(
    uid: Long,
    userOpt: Option[User]
  ): Free[F, Unit] =
    Free.inject(LogUserRetrieval(uid, userOpt))

  def logUserUpdate(
    uid: Long,
    opt: Option[Unit],
    newUser: User
  ): Free[F, Unit] =
    Free.inject(LogUserUpdate(uid, opt, newUser))

  def logUserDeletion(
    uid: Long,
    opt: Option[Unit]
  ): Free[F, Unit] =
    Free.inject(LogUserDeletion(uid, opt))
}
