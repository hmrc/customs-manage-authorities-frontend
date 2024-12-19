/*
 * Copyright 2024 HM Revenue & Customs
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

import models.AuthorityStart
import play.api.data.validation.{Constraint, Invalid, Valid}
import utils.StringUtils.emptyString

import java.time.LocalDate

trait Constraints {
  lazy val textFieldRegex: String = """^[^(){}$<>\[\]\\\/]*$"""
  lazy val gbnEoriRegex: String   = "GBN\\d{11}"
  lazy val eoriRegex: String      = "GB\\d{12}"
  lazy val xiEoriRegex: String    = "XI\\d{12}"

  protected def firstError[A](constraints: Constraint[A]*): Constraint[A] =
    Constraint { input =>
      constraints
        .map(_.apply(input))
        .find(_ != Valid)
        .getOrElse(Valid)
    }

  protected def minimumValue[A](minimum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint { input =>

      import ev._

      if (input >= minimum) {
        Valid
      } else {
        Invalid(errorKey, minimum)
      }
    }

  protected def maximumValue[A](maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint { input =>

      import ev._

      if (input <= maximum) {
        Valid
      } else {
        Invalid(errorKey, maximum)
      }
    }

  protected def inRange[A](minimum: A, maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint { input =>

      import ev._

      if (input >= minimum && input <= maximum) {
        Valid
      } else {
        Invalid(errorKey, minimum, maximum)
      }
    }

  protected def regexp(regex: String, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.matches(regex) =>
        Valid
      case _                         =>
        Invalid(errorKey, regex)
    }

  protected def maxLength(maximum: Int, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.length <= maximum =>
        Valid
      case _                            =>
        Invalid(errorKey, maximum)
    }

  protected def maxDate(maximum: LocalDate, errorKey: String, args: Any*): Constraint[LocalDate] =
    Constraint {
      case date if date.isAfter(maximum) =>
        Invalid(errorKey, args: _*)
      case _                             =>
        Valid
    }

  protected def maybeMaxDate(maybeMaximum: Option[LocalDate], errorKey: String, args: Any*): Constraint[LocalDate] =
    Constraint { date =>
      maybeMaximum match {
        case Some(value) if date.isAfter(value) => Invalid(errorKey, args: _*)
        case Some(_)                            => Valid
        case None                               => Valid
      }
    }

  protected def maybeMinDate(
    maybeMaximum: Option[LocalDate],
    errorKey: String,
    args: Any*
  ): Constraint[AuthorityStart] =
    Constraint { authorityStart =>
      maybeMaximum match {
        case Some(value) if authorityStart == AuthorityStart.Today && LocalDate.now().isAfter(value) =>
          Invalid(errorKey, args: _*)
        case Some(_)                                                                                 => Valid
        case None                                                                                    => Valid
      }
    }

  protected def minDate(minimum: LocalDate, minimumMsg: String, yearMsg: String, args: Any*): Constraint[LocalDate] =
    Constraint {
      case date if date.getYear.toString.length() != 4 =>
        Invalid(yearMsg, args: _*)

      case date if date.isBefore(minimum) =>
        Invalid(minimumMsg, args: _*)

      case _ => Valid
    }

  protected def nonEmptyList(errorKey: String): Constraint[List[_]] =
    Constraint {
      case list if list.nonEmpty =>
        Valid
      case _                     =>
        Invalid(errorKey)
    }

  protected def checkEORI(invalidFormatErrorKey: String): Constraint[String] =
    Constraint {
      case str if formatEORINumber(str).matches(gbnEoriRegex) => Valid
      case str if formatEORINumber(str).matches(eoriRegex)    => Valid
      case str if formatEORINumber(str).matches(xiEoriRegex)  => Valid
      case _                                                  => Invalid(invalidFormatErrorKey, eoriRegex)
    }

  protected def formatEORINumber(str: String): String =
    str.replaceAll("\\s", emptyString).toUpperCase
}
