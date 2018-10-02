/**
  * Copyright 2018 Harshad Deo
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
package typequuxtests

import typequux.constraint.TrueConstraint
import typequux.Typequux.{False, True}

class TrueConstraintSpec extends BaseSpec {

  it should "build properly" in {
    assertCompiles { """implicitly[TrueConstraint[True]]""" }
    assertCompiles { """def foo[U](implicit ev: U =:= True): TrueConstraint[U] = implicitly[TrueConstraint[U]]""" }
    assertCompiles { """def foo[U](implicit ev: True =:= U): TrueConstraint[U] = implicitly[TrueConstraint[U]]""" }

    assertTypeError { """implicitly[TrueConstraint[False]]""" }
  }

}
