package exercise.util

import java.nio.charset.Charset

import scala.concurrent.{ Future, Await }
import scala.concurrent.duration._
import scala.util.control.NonFatal

import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._

import akka.stream.scaladsl.StreamConverters

import argonaut.{ DecodeJson, Parse }

object CustomDirectives {

    // TODO make that one async using onComplete directive
    def extractWithArgonaut[T](implicit decoder: DecodeJson[T]): Directive1[T] =
      extractRequestContext.flatMap[Tuple1[T]] { ctx =>
        import ctx.executionContext
        import ctx.materializer
        val entity = ctx.request.entity

        entity.contentType match {
          case ContentTypes.`application/json` => {

            val timeout = FiniteDuration(3, SECONDS)

            val contentEither = 
              try { 
                Right {
                  Await.result(
                    entity
                      .toStrict(timeout)
                      .map(_.data.decodeString(Charset.forName("UTF-8"))),
                    timeout
                  )
                }
              } catch { 
                case NonFatal(t) => Left(t.getMessage) 
              }

            val t: Either[String, T] = (
              for {
                rawContent <- contentEither.right
                json <- Parse.parse(rawContent).right
                res <- decoder.decodeJson(json).result.right
              } yield res
            )
              .left
              .map(_.toString)


            t match {
              case Right(t) => Directive[Tuple1[T]] { inner => 
                inner(Tuple1(t)) 
              }
              case Left(errMsg) => Directive[Tuple1[T]] { inner => 
                complete(deserializationError(errMsg).toHttp(StatusCodes.BadRequest))
              }
            }
          }
          case ct => 
            complete(unsupportedContentType(ct).toHttp(StatusCodes.UnsupportedMediaType))
        }
      }

  def deserializationError(errorMsg: String) =
    ResponseMessage(
      status = "error", 
      desc = s"Deserialization error $errorMsg"
    )

  def unsupportedContentType(contentType: ContentType) =
    ResponseMessage(
      status = "error", 
      desc = s"""Content type $contentType is unsupported, use \"application/json\" instead"""
    )

}
