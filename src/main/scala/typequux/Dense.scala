/**
  * Copyright 2016 Harshad Deo
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package typequux

import Dense._
import language.higherKinds
import typequux._

/** Typelevel representation of dense numbers, stored as a list of [[Dense.Digit]]
  *
  * @author Harshad Deo
  * @since 0.1
  */
sealed trait Dense {

  type digit <: Digit
  type tail <: Dense

  type Inc <: Dense
  type Dec <: Dense

  type Add [b <: Dense] <: Dense
  type Mult[b <: Dense] = b#Match[Karatsuba[b, DNil], DNil, Dense]
  type Sq <: Dense
  type YodaExp[b <: Dense] = ExpHelper[b, _1] // to the power of two, raise base

  type ShiftL <: Dense
  type ShiftR <: Dense

  type Match [NonZero <: Up, IfZero <: Up, Up] <: Up
  type Compare[B <: Dense] = CompareC[B, EQ]

  protected type Len <: Dense

  protected type Karatsuba [x <: Dense, res <: Dense] <: Dense // Karatsuba/Russian Peasant/Egyptian multiplication
  protected type ExpHelper [arg <: Dense, res <: Dense] <: Dense
  protected type CompareC [B <: Dense, Carry <: Comparison] <: Comparison
}

/** Contains implementation for [[Dense]] and typeconstructor aliases that make usage more pleasant
  *
  * The operations can be shown to satisfy:
  *
  * 1. Additive commutativity: <code> +[A, B] =:= +[B, A]  </code>
  *
  * 2. Additive associativity: <code> +[A, +[B, C]] =:= +[+[A, B], C] </code>
  *
  * 3. Additive identity: <code> +[A, _0] =:= A =:= +[_0, A] </code>
  *
  * 4. Multiplicative commutativity: <code> *[A, B] =:= *[B, A] </code>
  *
  * 5. Multiplicative associativity: <code> *[A, *[B, C]] =:= *[*[A, B], C] </code>
  *
  * 6. Multiplicative identity: <code> *[A, _1] =:= A =:= *[_1, A] </code>
  *
  * 7. Distributivity: <code> *[A, +[B, C]] =:= +[*[A, B], *[A, C]] </code>
  *
  * 8. Zero exponent: <code> ^[A, _0] =:= _1 </code>
  *
  * 9. One exponent: <code> ^[_1, A] =:= _1 </code>
  *
  * 10. Exponent Identity: <code> ^[A, _1] =:= A </code> yes
  *
  * 11. Exponent combination 1: <code> *[^[A, B], ^[A, C]] =:= ^[A, *[B, C]] </code>
  *
  * 12. Exponent combination 2: <code> ^[^[A, B], C] =:= ^[A, *[B, C]] </code>
  *
  * 13. Exponent combination 3: <code> ^[*[A, B], C] =:= *[^[A, C], ^[B, C]] </code>
  *
  * 14. Total Order
  *
  * @author Harshad Deo
  * @since 0.1
  */
object Dense {

  /** Represents a digit in the dense encoding of a natural number
    *
    * @author Harshad Deo
    * @since 0.1
    */
  sealed trait Digit {
    type Match [IfOne <: Up, IfZero <: Up, Up] <: Up
    type Compare [D <: Digit] <: Comparison
  }

  /** Represents a 0 in the dense encoding of a natural number
    *
    * @author Harshad Deo
    * @since 0.1
    */
  trait D0 extends Digit {
    override type Match[IfOne <: Up, IfZero <: Up, Up] = IfZero
    override type Compare[D <: Digit] = D#Match[LT, EQ, Comparison]
  }

  /** Represents a 1 in the dense encoding of a natural number
    *
    * @author Harshad Deo
    * @since 0.1
    */
  trait D1 extends Digit {
    override type Match[IfOne <: Up, IfZero <: Up, Up] = IfOne
    override type Compare[D <: Digit] = D#Match[EQ, GT, Comparison]
  }

  /** Non-zero dense number. The digit is the least significant bit
    *
    * @author Harshad Deo
    * @since 0.1
    */
  trait DCons[d <: Dense.Digit, T <: Dense] extends Dense {

    override type digit = d
    override type tail = T

    override type Inc = d#Match[D0 :: T#Inc, D1 :: T, Dense]
    override type Dec = d#Match[T#Match[D0 :: T, DNil, Dense], D1 :: T#Dec, Dense]
    override type Add[b <: Dense] = b#Match[AddNz[b], d :: T, Dense]
    override type Sq = *[d :: T, d :: T]

    override type ShiftR = tail
    override type ShiftL = D0 :: DCons[d, T]

    override type Match[NonZero <: Up, IfZero <: Up, Up] = NonZero

    protected type AddNz[b <: Dense] = d#Match[Add1[b], b#digit :: tail#Add[b#tail], Dense]
    protected type Add1[b <: Dense] = b#digit#Match[D0 :: tail#Add[b#tail]#Inc, d :: tail#Add[b#tail], Dense]
    protected type NewCarry[prev <: Comparison, od <: Digit] = d#Compare[od]#Match[LT, prev, GT, Comparison]
    override protected type Karatsuba[x <: Dense, res <: Dense] =
      tail#Karatsuba[x#ShiftL, digit#Match[x + res, res, Dense]]
    override protected type ExpHelper[arg <: Dense, res <: Dense] =
      tail#ExpHelper[arg#Sq, digit#Match[*[res, arg], res, Dense]]
    override protected type CompareC[B <: Dense, Carry <: Comparison] =
      B#Match[tail#CompareC[B#tail, NewCarry[Carry, B#digit]], GT, Comparison]
    override protected type Len = _1 + tail#Len
  }

  /** Dense Zero
    *
    * @author Harshad Deo
    * @since 0.1
    */
  object DNil extends Dense {

    override type tail = Nothing
    override type digit = Nothing

    override type Inc = D1 :: DNil
    override type Dec = Nothing
    override type Add[b <: Dense] = b
    override type Sq = _0

    override type ShiftR = DNil
    override type ShiftL = DNil

    override type Match[NonZero <: Up, IfZero <: Up, Up] = IfZero

    override protected type Karatsuba[x <: Dense, res <: Dense] = res
    override protected type ExpHelper[arg <: Dense, res <: Dense] = res
    override protected type CompareC[B <: Dense, Carry <: Comparison] = B#Match[LT, Carry, Comparison]
    override protected type Len = _0
  }

  type ::[H <: Digit, T <: Dense] = DCons[H, T]

  type +[A <: Dense, B <: Dense] = A#Add[B]
  type *[A <: Dense, B <: Dense] = A#Mult[B]
  type ^[A <: Dense, B <: Dense] = B#YodaExp[A]

  type Compare[A <: Dense, B <: Dense] = A#Compare[B]
  type ===[A <: Dense, B <: Dense] = A#Compare[B]#eq
  type <[A <: Dense, B <: Dense] = A#Compare[B]#lt
  type <=[A <: Dense, B <: Dense] = A#Compare[B]#le
  type >[A <: Dense, B <: Dense] = A#Compare[B]#gt
  type >=[A <: Dense, B <: Dense] = A#Compare[B]#ge
  type Sq[A <: Dense] = A#Sq
  type Max[A <: Dense, B <: Dense] = A#Compare[B]#Match[B, A, A, Dense]
  type Min[A <: Dense, B <: Dense] = A#Compare[B]#Match[A, A, B, Dense]

  type _0 = DNil
  type _1 = D1 :: DNil
  type _2 = D0 :: D1 :: DNil
  type _3 = D1 :: D1 :: DNil
  type _4 = D0 :: D0 :: D1 :: DNil
  type _5 = D1 :: D0 :: D1 :: DNil
  type _6 = D0 :: D1 :: D1 :: DNil
  type _7 = D1 :: D1 :: D1 :: DNil
  type _8 = D0 :: D0 :: D0 :: D1 :: DNil
  type _9 = D1 :: D0 :: D0 :: D1 :: DNil
  type _10 = D0 :: D1 :: D0 :: D1 :: DNil
  type _11 = D1 :: D1 :: D0 :: D1 :: DNil
  type _12 = D0 :: D0 :: D1 :: D1 :: DNil
  type _13 = D1 :: D0 :: D1 :: D1 :: DNil
  type _14 = D0 :: D1 :: D1 :: D1 :: DNil
  type _15 = D1 :: D1 :: D1 :: D1 :: DNil
  type _16 = D0 :: D0 :: D0 :: D0 :: D1 :: DNil
  type _17 = D1 :: D0 :: D0 :: D0 :: D1 :: DNil
  type _18 = D0 :: D1 :: D0 :: D0 :: D1 :: DNil
  type _19 = D1 :: D1 :: D0 :: D0 :: D1 :: DNil
  type _20 = D0 :: D0 :: D1 :: D0 :: D1 :: DNil
  type _21 = D1 :: D0 :: D1 :: D0 :: D1 :: DNil
  type _22 = D0 :: D1 :: D1 :: D0 :: D1 :: DNil

  /** Builda a value level representation of a dense type.
    *
    * @author Harshad Deo
    * @since 0.1
    */
  class DenseRep[D](val v: Long)(implicit ev: D <:< Dense)

  /** Contains implicit definitions to build the value level representation of a dense type
    *
    * @author Harshad Deo
    * @since 0.1
    */
  object DenseRep {

    implicit object DNil2Rep extends DenseRep[DNil](0)
    implicit def dCons02Rep[T <: Dense](implicit tr: DenseRep[T]): DenseRep[D0 :: T] = new DenseRep(tr.v << 1)
    implicit def dCons22Rep[T <: Dense](implicit tr: DenseRep[T]): DenseRep[D1 :: T] = new DenseRep((tr.v << 1) | 1)
  }

  def toLong[D <: Dense](implicit dr: DenseRep[D]): Long = dr.v
}

/** Marker trait for typelevel subtraction of [[Dense]] numbers.
  *
  * Takes linear time (compared to log time for addition), therefore its usage should be limited
  *
  * @tparam M Minuend
  * @tparam S Subtrahend
  * @tparam D Difference
  * 
  * @author Harshad Deo
  * @since 0.1
  */
sealed trait DenseDiff[M, S, D]

/** Contains implicit definitions to build a [[DenseDiff]] marker
  *
  * @author Harshad Deo
  * @since 0.1
  */
object DenseDiff {
  implicit def dsr0[M <: Dense]: DenseDiff[M, _0, M] = new DenseDiff[M, _0, M] {}
  implicit def dsrN[M <: Dense, S <: Dense, DP <: Dense](
      implicit ev: DenseDiff[M#Dec, S#Dec, DP], ev1: True =:= >[S, _0]): DenseDiff[M, S, DP] =
    new DenseDiff[M, S, DP] {}
}
