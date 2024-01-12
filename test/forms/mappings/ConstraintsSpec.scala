/*
 * Copyright 2023 HM Revenue & Customs
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

import generators.Generators
import org.scalacheck.Gen
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.validation.{Invalid, Valid}

import java.time.LocalDate

class ConstraintsSpec extends WordSpec with MustMatchers with ScalaCheckPropertyChecks with Generators with Constraints {


  "firstError" must {

    "return Valid when all constraints pass" in new Setup {
      val result = firstError(maxLength(maxVal, errLengthKey), regexp("""^\w+$""", errRegexKey))("foo")
      result mustEqual Valid
    }

    "return Invalid when the first constraint fails" in new Setup {
      val result = firstError(maxLength(maxVal, errLengthKey), regexp("""^\w+$""", errRegexKey))("a" * 11)
      result mustEqual Invalid(errLengthKey, maxVal)
    }

    "return Invalid when the second constraint fails" in new Setup {
      val result = firstError(maxLength(maxVal, errLengthKey), regexp("""^\w+$""", errRegexKey))("")
      result mustEqual Invalid(errRegexKey, """^\w+$""")
    }

    "return Invalid for the first error when both constraints fail" in new Setup {
      val result = firstError(maxLength(-1, errLengthKey), regexp("""^\w+$""", errRegexKey))("")
      result mustEqual Invalid(errLengthKey, -1)
    }
  }

  "minimumValue" must {

    "return Valid for a number greater than the threshold" in new Setup {
      val result = minimumValue(minVal, "error.min").apply(2)
      result mustEqual Valid
    }

    "return Valid for a number equal to the threshold" in new Setup {
      val result = minimumValue(minVal, "error.min").apply(1)
      result mustEqual Valid
    }

    "return Invalid for a number below the threshold" in new Setup {
      val result = minimumValue(minVal, "error.min").apply(0)
      result mustEqual Invalid("error.min", 1)
    }
  }

  "maximumValue" must {

    "return Valid for a number less than the threshold" in new Setup {
      val result = maximumValue(1, "error.max").apply(0)
      result mustEqual Valid
    }

    "return Valid for a number equal to the threshold" in new Setup {
      val result = maximumValue(1, "error.max").apply(1)
      result mustEqual Valid
    }

    "return Invalid for a number above the threshold" in new Setup {
      val result = maximumValue(1, "error.max").apply(2)
      result mustEqual Invalid("error.max", 1)
    }
  }


  "InRange" must {

    "return Valid for a number within range" in new Setup {
      val result = inRange(minVal, maxVal, "error.invalid").apply(2)
      result mustEqual Valid
    }

    "return InValid for a number outside range" in new Setup {
      val result = inRange(minVal, maxVal, "error.invalid").apply(12)
      result mustEqual Invalid("error.invalid", 1, maxVal)
    }

  }

  "regexp" must {

    "return Valid for an input that matches the expression" in {
      val result = regexp("""^\w+$""", "error.invalid")("foo")
      result mustEqual Valid
    }

    "return Invalid for an input that does not match the expression" in {
      val result = regexp("""^\d+$""", "error.invalid")("foo")
      result mustEqual Invalid("error.invalid", """^\d+$""")
    }
  }

  "maxLength" must {

    "return Valid for a string shorter than the allowed length" in new Setup {
      val result = maxLength(maxVal, errLengthKey)("a" * 9)
      result mustEqual Valid
    }

    "return Valid for an empty string" in new Setup {
      val result = maxLength(maxVal, errLengthKey)("")
      result mustEqual Valid
    }

    "return Valid for a string equal to the allowed length" in new Setup {
      val result = maxLength(maxVal, errLengthKey)("a" * 10)
      result mustEqual Valid
    }

    "return Invalid for a string longer than the allowed length" in new Setup {
      val result = maxLength(maxVal, errLengthKey)("a" * 11)
      result mustEqual Invalid(errLengthKey, maxVal)
    }
  }

  "maxDate" must {

    "return Valid for a date before or equal to the maximum" in new Setup {

      val gen: Gen[(LocalDate, LocalDate)] = for {
        max <- datesBetween(LocalDate.of(2000, 1, 1), LocalDate.of(3000, 1, 1))
        date <- datesBetween(LocalDate.of(2000, 1, 1), max)
      } yield (max, date)

      forAll(gen) {
        case (max, date) =>

          val result = maxDate(max, "error.future")(date)
          result mustEqual Valid
      }
    }

    "return Invalid for a date after the maximum" in new Setup {

      val gen: Gen[(LocalDate, LocalDate)] = for {
        max <- datesBetween(LocalDate.of(2000, 1, 1), LocalDate.of(3000, 1, 1))
        date <- datesBetween(max.plusDays(1), LocalDate.of(3000, 1, 2))
      } yield (max, date)

      forAll(gen) {
        case (max, date) =>

          val result = maxDate(max, "error.future", "foo")(date)
          result mustEqual Invalid("error.future", "foo")
      }
    }
  }

  "minDate" must {

    "return Valid for a date after or equal to the minimum" in new Setup {

      val gen: Gen[(LocalDate, LocalDate)] = for {
        min <- datesBetween(LocalDate.of(2000, 1, 1), LocalDate.of(3000, 1, 1))
        date <- datesBetween(min, LocalDate.of(3000, 1, 1))
      } yield (min, date)

      forAll(gen) {
        case (min, date) =>

          val result = minDate(min, "error.past", "foo")(date)
          result mustEqual Valid
      }
    }

    "return Invalid for a date before the minimum" in new Setup {

      val gen: Gen[(LocalDate, LocalDate)] = for {
        min <- datesBetween(LocalDate.of(2000, 1, 2), LocalDate.of(3000, 1, 1))
        date <- datesBetween(LocalDate.of(2000, 1, 1), min.minusDays(1))
      } yield (min, date)

      forAll(gen) {
        case (min, date) =>
          val result = minDate(min, "authorityStartDate.error.minimum",
            "authorityStartDate.error.year.length", "foo")(date)
          result mustEqual Invalid("authorityStartDate.error.minimum", "foo")
      }
    }

    "return Invalid for a date below the minimum length" in new Setup {

      val date = LocalDate.of(200, 1, 2)
      val min = LocalDate.of(200, 1, 1)
      val result = minDate(min, "authorityStartDate.error.minimum",
        "authorityStartDate.error.year.length", "foo")(date)
      result mustEqual Invalid("authorityStartDate.error.year.length", "foo")
    }

    "return Invalid for a date above the max length" in new Setup {

      val date = LocalDate.of(20000, 1, 2)
      val max = LocalDate.of(20000, 1, 1)
      val result = minDate(max, "authorityStartDate.error.minimum",
        "authorityStartDate.error.year.length", "foo")(date)
      result mustEqual Invalid("authorityStartDate.error.year.length", "foo")
    }
  }


  "checkEORI" must {

    "return valid when GBN EORI is provided" in {
      val result = checkEORI("error.invalid2")("GBN45365789211")
      result mustEqual Valid
    }

    "return Invalid when an incorrect EORI format is provided" in {
      val result = checkEORI("error.invalid2")("XI453")
      result mustEqual Invalid("error.invalid2", """GB\d{12}""")
    }

    "return Valid for an input that does not match the expression" in {
      val result = checkEORI("error.invalid2")("GB123456789102")
      result mustEqual Valid
    }
  }
}

trait Setup {

  val maxVal = 10
  val minVal = 1
  val errLengthKey = "error.length"
  val errRegexKey = "error.regexp"
}
