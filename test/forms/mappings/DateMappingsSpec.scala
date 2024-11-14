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

import base.SpecBase
import generators.Generators
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.{Form, FormError}
import utils.StringUtils.emptyString

import java.time.LocalDate

class DateMappingsSpec extends SpecBase
  with ScalaCheckPropertyChecks
  with Generators
  with OptionValues
  with Mappings {

  "bind valid data" in new Setup {

    forAll(validData -> "valid date") {
      date =>

        val data = Map(
          "value.day" -> date.getDayOfMonth.toString,
          "value.month" -> date.getMonthValue.toString,
          "value.year" -> date.getYear.toString
        )

        val result = form.bind(data)

        result.value.value mustEqual date
    }
  }

  "fail to bind an empty date" in new Setup {

    val result: Form[LocalDate] = form.bind(Map.empty[String, String])

    result.errors must contain only FormError("value.day", "error.required.all", List.empty)
  }

  "fail to bind a date with a missing day" in new Setup {

    forAll(validData -> "valid date", missingField -> "missing field") {
      (date, field) =>

        val initialData = Map(
          "value.month" -> date.getMonthValue.toString,
          "value.year" -> date.getYear.toString
        )

        val data = field.fold(initialData) {
          value =>
            initialData + ("value.day" -> value)
        }

        val result = form.bind(data)

        result.errors must contain only FormError("value.day", "error.required", List("day"))
    }
  }

  "fail to bind a date with an invalid day" in new Setup {

    forAll(validData -> "valid date", invalidField -> "invalid field") {
      (date, field) =>

        val data = Map(
          "value.day" -> field,
          "value.month" -> date.getMonthValue.toString,
          "value.year" -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain(
          FormError("value.day", "error.invalid", List.empty)
        )
    }
  }

  "fail to bind a date with an invalid month" in new Setup {

    forAll(validData -> "valid data", invalidField -> "invalid field") {
      (date, field) =>

        val data = Map(
          "value.day" -> date.getDayOfMonth.toString,
          "value.month" -> field,
          "value.year" -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain(
          FormError("value.month", "error.invalid", List.empty)
        )
    }
  }

  "fail to bind a date with an invalid year" in new Setup {

    forAll(validData -> "valid data", invalidField -> "invalid field") {
      (date, field) =>

        val data = Map(
          "value.day" -> date.getDayOfMonth.toString,
          "value.month" -> date.getMonthValue.toString,
          "value.year" -> field
        )

        val result = form.bind(data)

        result.errors must contain(
          FormError("value.year", "error.invalid", List.empty)
        )
    }
  }

  "fail to bind an invalid day and month" in new Setup {

    forAll(validData -> "valid date", invalidField -> "invalid day", invalidField -> "invalid month") {
      (date, day, month) =>

        val data = Map(
          "value.day" -> day,
          "value.month" -> month,
          "value.year" -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value.day", "error.invalid", List.empty)
    }
  }

  "fail to bind an invalid day and year" in new Setup {

    forAll(validData -> "valid date", invalidField -> "invalid day", invalidField -> "invalid year") {
      (date, day, year) =>

        val data = Map(
          "value.day" -> day,
          "value.month" -> date.getMonthValue.toString,
          "value.year" -> year
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value.day", "error.invalid", List.empty)
    }
  }

  "fail to bind an invalid month and year" in new Setup {

    forAll(validData -> "valid date", invalidField -> "invalid month", invalidField -> "invalid year") {
      (date, month, year) =>

        val data = Map(
          "value.day" -> date.getDayOfMonth.toString,
          "value.month" -> month,
          "value.year" -> year
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value.month", "error.invalid", List.empty)
    }
  }

  "fail to bind an invalid day, month and year" in new Setup {

    forAll(invalidField -> "valid day", invalidField -> "invalid month", invalidField -> "invalid year") {
      (day, month, year) =>

        val data = Map(
          "value.day" -> day,
          "value.month" -> month,
          "value.year" -> year
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value.day", "error.invalid", List.empty)
    }
  }

  "fail to bind an invalid date" in new Setup {

    val data = Map(
      "value.day" -> "30",
      "value.month" -> "2",
      "value.year" -> "2018"
    )

    val result = form.bind(data)

    result.errors must contain(
      FormError("value.day", "error.invalid", List.empty)
    )
  }

  "unbind a date" in new Setup {

    forAll(validData -> "valid date") {
      date =>

        val filledForm = form.fill(date)

        filledForm("value.day").value.value mustEqual date.getDayOfMonth.toString
        filledForm("value.month").value.value mustEqual date.getMonthValue.toString
        filledForm("value.year").value.value mustEqual date.getYear.toString
    }
  }

  trait Setup {
    val year2000 = 2000
    val year3000 = 3000
    val monthOfTheYear = 1
    val dayOfTheMonth = 1

    val form: Form[LocalDate] = Form(
      "value" -> localDate(
        requiredKey = "error.required",
        allRequiredKey = "error.required.all",
        twoRequiredKey = "error.required.two",
        invalidKey = "error.invalid"
      )
    )

    val validData: Gen[LocalDate] = datesBetween(
      min = LocalDate.of(year2000, monthOfTheYear, dayOfTheMonth),
      max = LocalDate.of(year3000, monthOfTheYear, dayOfTheMonth)
    )

    val invalidField: Gen[String] = Gen.alphaStr.suchThat(_.nonEmpty)

    val missingField: Gen[Option[String]] = Gen.option(Gen.const(emptyString))
  }

}
