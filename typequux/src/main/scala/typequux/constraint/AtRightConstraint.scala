/**
  * Copyright 2019 Harshad Deo
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

/** Typeclass to get an element from an object that supports sequential indexing, like [[HList]] and Tuples.
  * By convention, the index is 0-based and from the right or the end.
  *
  * @tparam N Type of the index
  * @tparam HL Type of the object from which the element is retrieved
  * @tparam A Type of the result
  *
  * @author Harshad Deo
  * @since 0.1
  */
trait AtRightConstraint[N, HL, A] {
  def apply(hl: HL): A
}