package exercise.algebra

import scala.language.higherKinds

import akka.http.scaladsl.model._

import cats.free.{ Free, Inject }

import exercise.model.User

sealed trait SerializationOp[T]
case class DeserializeUser(entity: HttpEntity) extends SerializationOp[Either[DeserializationError, User]]
case class SerializeUser(user: User) extends SerializationOp[String]

class SerializationOps[F[_]](implicit ev: Inject[SerializationOp, F]) {

  def deserializeUser(entity: HttpEntity): Free[F, Either[DeserializationError, User]] =
    Free.inject(DeserializeUser(entity))

  def serializeUser(user: User): Free[F, String] =
    Free.inject(SerializeUser(user))
}

sealed trait DeserializationError
case class UnsupportedContentType(contentType: ContentType) extends DeserializationError
case class InvalidJson(details: String) extends DeserializationError
