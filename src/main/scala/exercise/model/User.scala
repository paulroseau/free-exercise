package exercise.model

import argonaut._, Argonaut._, ArgonautShapeless._

case class User(name: String, age: Int)

object User {
  val encoder = EncodeJson.of[User]
  val decoder = DecodeJson.of[User]
}
