package exercise.interpreter

import scala.concurrent.{ ExecutionContext, Future }

import cats.Id
import cats.arrow.FunctionK

import exercise.algebra._

object SerializerInterpreter {

  val futureInterpreter: FunctionK[SerializationOp, Future] =
    new FunctionK[SerializationOp, Future] {

      def apply[T](op: SerializationOp[T]): Future[T] =
        op match {
          case DeserializeUser(entity) => ???
          case SerializeUser(user) => ???
        }
    }
}
