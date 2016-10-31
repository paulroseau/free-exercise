package exercise.algebra

import scala.language.higherKinds

import akka.http.scaladsl.model._

import argonaut._

import cats.free.{ Free, Inject }

sealed trait SerializationOp[T]
case class DeserializeEntity[A](entity: HttpEntity, decoder: DecodeJson[A]) extends SerializationOp[Either[DeserializationError, A]]
case class Serialize[A](a: A, encoder: EncodeJson[A]) extends SerializationOp[String]

class SerializationOps[F[_]](implicit ev: Inject[SerializationOp, F]) {

  def deserialize[A](entity: HttpEntity)(implicit
    decoder: DecodeJson[A]
  ): Free[F, Either[DeserializationError, A]] =
    Free.inject(DeserializeEntity(entity, decoder): SerializationOp[Either[DeserializationError, A]])

  def serialize[A : EncodeJson](a: A)(implicit
    encoder: EncodeJson[A]
  ): Free[F, String] =
    Free.inject(Serialize(a, encoder): SerializationOp[String])
}

sealed trait DeserializationError
case class UnsupportedContentType(contentType: ContentType) extends DeserializationError
case class InvalidJson(details: String) extends DeserializationError
