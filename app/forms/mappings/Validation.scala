/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package forms.mappings

import scala.util.{Success, Try}

object Validation {

  val isEmpty: String => Boolean                          = (input: String) => input.trim.isEmpty
  val isEmptyOr: (String => Boolean) => String => Boolean = (f: String => Boolean) =>
    (input: String) => isEmpty(input) || f(input.trim)
  val nonEmpty: String => Boolean                         = (input: String) => input.trim.nonEmpty

  val isInRange: (Int, Int) => String => Boolean = (min: Int, max: Int) =>
    (input: String) =>
      Try(input.trim.toInt) match {
        case Success(value) => value >= min && value <= max
        case _              => false
      }
}
