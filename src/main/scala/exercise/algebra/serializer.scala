package exercise.algebra

import scala.language.higherKinds

import akka.http.scaladsl.model.{ HttpEntity, HttpResponse }

import cats.free.{ Free, Inject }

import exercise.model.User

sealed trait SerializationOp[T]
case class DeserializeUser(entity: HttpEntity) extends SerializationOp[Either[String, User]]
case class SerializeUser(user: User) extends SerializationOp[String]

class SerializationOps[F[_]](implicit ev: Inject[SerializationOp, F]) {

  def deserializeUser(entity: HttpEntity): Free[F, Either[String, User]] =
    Free.inject(DeserializeUser(entity))

  def serializeUser(user: User): Free[F, String] =
    Free.inject(SerializeUser(user))
}
