package exercise.util

import akka.http.scaladsl.model._
import argonaut._, Argonaut._, ArgonautShapeless._

case class ResponseMessage(status: String, desc: String) {

  def toHttp(status: StatusCode): HttpResponse =
    HttpResponse(
      status = status, 
      entity = HttpEntity(
        ContentTypes.`application/json`,
        this.asJson.spaces2
      )
    )
}
