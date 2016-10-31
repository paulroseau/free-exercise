package exercise.interpreter

import java.nio.charset.Charset

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._

import argonaut._, Argonaut._

import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.stream.scaladsl.StreamConverters

import cats.arrow.FunctionK

import exercise.algebra._

object SerializerInterpreter {

  def futureInterpreter(implicit
    ec: ExecutionContext,
    materializer: Materializer
  ): FunctionK[SerializationOp, Future] =
    new FunctionK[SerializationOp, Future] {

      val timeout = FiniteDuration(3, SECONDS)

      def apply[T](op: SerializationOp[T]): Future[T] =
        op match {

          case Serialize(a, encoder) => Future(encoder.encode(a).spaces2)

          case DeserializeEntity(entity, decoder) =>
            entity.contentType match {
              case ContentTypes.`application/json` =>
                entity
                  .toStrict(timeout)
                  .map { x => 
                    val contentEither =
                      Right(x.data.decodeString(Charset.forName("UTF-8")))

                    (for {
                      rawContent <- contentEither.right
                      json <- Parse.parse(rawContent).right
                      res <- decoder.decodeJson(json).result.right
                    } yield res)
                      .left
                      .map(err => InvalidJson(err.toString))
                  }
            case ct => 
              Future(Left(UnsupportedContentType(ct)))
            }
        }
  }
}
