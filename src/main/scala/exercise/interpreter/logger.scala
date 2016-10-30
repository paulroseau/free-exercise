package exercise.interpreter

import scala.concurrent.{ ExecutionContext, Future }

import cats.Id
import cats.arrow.FunctionK

import exercise.algebra._

object StoreLoggingInterpreter {

  val idInterpreter: FunctionK[StoreLoggingOp, Id] =
    new FunctionK[StoreLoggingOp, Id] {

      def apply[T](logOp: StoreLoggingOp[T]): Id[T] =
        logOp match {
          case LogUserCreation(uid, user) => 
            Console.out.println(s"Creation of user #${uid} : $user")
          case LogUserRetrieval(uid, userOpt) =>
            userOpt match {
              case Some(_) => 
                Console.out.println(s"Fetched user #${uid} successfully")
              case None =>
                Console.err.println(s"User #${uid} not found")
            }
          case LogUserUpdate(uid, opt, newUser) =>
            opt match {
              case Some(_) => 
                Console.out.println(s"Update user #${uid} now worth ${newUser}")
              case None =>
                Console.err.println(s"Could not update user #${uid} (user not found)")
            }
          case LogUserDeletion(uid, opt) =>
            opt match {
              case Some(_) => 
                Console.out.println(s"Delete user #${uid} successfully")
              case None =>
                Console.err.println(s"Could not delete user #${uid} (user not found)")
            }
        }
    }

  def futureInterpreter(implicit ec: ExecutionContext): FunctionK[StoreLoggingOp, Future] =
    new FunctionK[StoreLoggingOp, Future] {

      def apply[T](logOp: StoreLoggingOp[T]): Future[T] =
        logOp match {
          case LogUserCreation(uid, user) => 
            Future(
              Console.out.println(s"Creation of user #${uid} : $user")
            )
          case LogUserRetrieval(uid, userOpt) =>
            userOpt match {
              case Some(_) => 
                Future(
                  Console.out.println(s"Fetched user #${uid} successfully")
                )
              case None =>
                Future(
                  Console.err.println(s"User #${uid} not found")
                )
            }
          case LogUserUpdate(uid, opt, newUser) =>
            opt match {
              case Some(_) => 
                Future(
                  Console.out.println(s"Update user #${uid} now worth ${newUser}")
                )
              case None =>
                Future(
                  Console.err.println(s"Could not update user #${uid} (user not found)")
                )
            }
          case LogUserDeletion(uid, opt) =>
            opt match {
              case Some(_) => 
                Future(
                  Console.out.println(s"Delete user #${uid} successfully")
                )
              case None =>
                Future(
                  Console.err.println(s"Could not delete user #${uid} (user not found)")
                )
            }
        }
    }
}
