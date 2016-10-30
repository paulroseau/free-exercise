package exercise.interpreter

import scala.language.higherKinds

import cats.arrow.FunctionK
import cats.data.Coproduct

object Interpreter {

  def apply[F[_], G[_]](implicit interpreter: FunctionK[F, G]): FunctionK[F, G] =
    interpreter

  implicit def coproductInterpreter[F[_], G[_], H[_]](implicit 
    leftInterpreter: FunctionK[F, H],
    rightInterpreter: FunctionK[G, H]
  ): FunctionK[Coproduct[F, G, ?], H] =
    leftInterpreter or rightInterpreter
}
