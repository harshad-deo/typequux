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
package typequux.constraint

import typequux._
import typequux._

sealed trait UpdatedConstraint[N, HL, A, R] {
  def apply(hl: HL, a: A): R
}

object UpdatedConstraint {

  implicit def hlUpdatedConstraint[N, HL <: HList, A, R, Before <: HList, _, After <: HList](
      implicit ev0: PIndexer[N, HL, Before, _, After],
      ev1: AppendConstraint[Before, A :+: After, R]): UpdatedConstraint[N, HL, A, R] =
    new UpdatedConstraint[N, HL, A, R] {
      override def apply(hl: HL, a: A) = {
        val (before, _, after) = ev0(hl)
        ev1(before, a :+: after)
      }
    }

  implicit def tpUpdatedConstraint[N, T, A, R, HL <: HList, HLR <: HList](
      implicit ev0: Tuple2HListConverter[T, HL],
      ev1: UpdatedConstraint[N, HL, A, HLR],
      ev2: HList2TupleConverter[R, HLR]): UpdatedConstraint[N, T, A, R] = new UpdatedConstraint[N, T, A, R] {
    override def apply(t: T, a: A) = {
      val hl = ev0(t)
      val hla = ev1(hl, a)
      ev2(hla)
    }
  }
}
