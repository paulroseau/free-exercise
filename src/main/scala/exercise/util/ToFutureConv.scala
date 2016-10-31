package exercise.util

import scala.language.higherKinds
import scala.concurrent.{ Future, ExecutionContext }

import cats.Id

trait ToFutureConv[F[_]] {
  def apply[T](f: F[T]): Future[T]
}

object ToFutureConv {

  implicit def idToFuture(implicit ec: ExecutionContext) = 
    new ToFutureConv[Id] {
      def apply[T](f: Id[T]): Future[T] = Future(f)
    }

  implicit val futureToFuture = new ToFutureConv[Future] {
    def apply[T](f: Future[T]): Future[T] = f
  }
}
