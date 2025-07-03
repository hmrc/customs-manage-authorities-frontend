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
import scala.collection.immutable.ArraySeq

class AuthorityDateMappingSpec
    extends SpecBase
    with ScalaCheckPropertyChecks
    with Generators
    with OptionValues
    with Mappings {

  "bind valid data" in new Setup {

    forAll(validData -> "valid date") { date =>

      val data = Map(
        "day"   -> date.getDayOfMonth.toString,
        "month" -> date.getMonthValue.toString,
        "year"  -> date.getYear.toString
      )

      val result = form.bind(data)

      result.value.value mustEqual date
    }
  }

  "fail to bind an empty date" in new Setup {

    val result: Form[LocalDate] = form.bind(
      Map(
        "day"   -> emptyString,
        "month" -> emptyString,
        "year"  -> emptyString
      )
    )

    result.errors must contain only FormError("", "authorityStartDate.error.required.all", List.empty)
  }

  "fail to bind a date with a missing day" in new Setup {

    forAll(validData -> "valid date", missingField -> "missing field") { (date, field) =>

      val initialData = Map(
        "day"   -> field,
        "month" -> date.getMonthValue.toString,
        "year"  -> date.getYear.toString
      )

      val result = form.bind(initialData)

      result.errors must contain only FormError("day", "authorityDate.error.required.day", ArraySeq.empty[String])
    }
  }

  "fail to bind a date with an invalid day" in new Setup {

    forAll(validData -> "valid date", invalidField -> "invalid field") { (date, field) =>

      val data = Map(
        "day"   -> field,
        "month" -> date.getMonthValue.toString,
        "year"  -> date.getYear.toString
      )

      val result = form.bind(data)

      result.errors must contain(
        FormError("day", "authorityDate.error.invalid.day", List.empty)
      )
    }
  }

  "fail to bind a date with an invalid month" in new Setup {

    forAll(validData -> "valid data", invalidField -> "invalid field") { (date, field) =>

      val data = Map(
        "day"   -> date.getDayOfMonth.toString,
        "month" -> field,
        "year"  -> date.getYear.toString
      )

      val result = form.bind(data)

      result.errors must contain(
        FormError("month", "authorityDate.error.invalid.month", List.empty)
      )
    }
  }

  "fail to bind a date with an invalid year" in new Setup {

    forAll(validData -> "valid data", invalidField -> "invalid field") { (date, field) =>

      val data = Map(
        "day"   -> date.getDayOfMonth.toString,
        "month" -> date.getMonthValue.toString,
        "year"  -> field
      )

      val result = form.bind(data)

      result.errors must contain(
        FormError("year", "authorityDate.error.invalid.year", List.empty)
      )
    }
  }

  "fail to bind an invalid day and month" in new Setup {

    forAll(validData -> "valid date", invalidField -> "invalid day", invalidField -> "invalid month") {
      (date, day, month) =>

        val data = Map(
          "day"   -> day,
          "month" -> month,
          "year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain theSameElementsAs Seq(
          FormError("day", "authorityDate.error.invalid.day", List.empty),
          FormError("month", "authorityDate.error.invalid.month", List.empty)
        )
    }
  }

  "fail to bind an invalid day and year" in new Setup {

    forAll(validData -> "valid date", invalidField -> "invalid day", invalidField -> "invalid year") {
      (date, day, year) =>

        val data = Map(
          "day"   -> day,
          "month" -> date.getMonthValue.toString,
          "year"  -> year
        )

        val result = form.bind(data)

        result.errors must contain theSameElementsAs Seq(
          FormError("day", "authorityDate.error.invalid.day", List.empty),
          FormError("year", "authorityDate.error.invalid.year", List.empty)
        )
    }
  }

  "fail to bind an invalid month and year" in new Setup {

    forAll(validData -> "valid date", invalidField -> "invalid month", invalidField -> "invalid year") {
      (date, month, year) =>

        val data = Map(
          "day"   -> date.getDayOfMonth.toString,
          "month" -> month,
          "year"  -> year
        )

        val result = form.bind(data)

        result.errors must contain theSameElementsAs Seq(
          FormError("month", "authorityDate.error.invalid.month", List.empty),
          FormError("year", "authorityDate.error.invalid.year", List.empty)
        )
    }
  }

  "fail to bind an invalid day, month and year" in new Setup {

    forAll(invalidField -> "valid day", invalidField -> "invalid month", invalidField -> "invalid year") {
      (day, month, year) =>

        val data = Map(
          "day"   -> day,
          "month" -> month,
          "year"  -> year
        )

        val result = form.bind(data)

        result.errors must contain theSameElementsAs Seq(
          FormError("day", "authorityDate.error.invalid.day", List.empty),
          FormError("month", "authorityDate.error.invalid.month", List.empty),
          FormError("year", "authorityDate.error.invalid.year", List.empty)
        )
    }
  }

  "fail to bind an invalid date" in new Setup {

    val data = Map(
      "day"   -> "30",
      "month" -> "2",
      "year"  -> "2018"
    )

    val result = form.bind(data)

    result.errors must contain(
      FormError("", "authorityDate.error.invalid", List.empty)
    )
  }

  "unbind a date" in new Setup {

    forAll(validData -> "valid date") { date =>

      val filledForm = form.fill(date)

      filledForm("day").value.value mustEqual date.getDayOfMonth.toString
      filledForm("month").value.value mustEqual date.getMonthValue.toString
      filledForm("year").value.value mustEqual date.getYear.toString
    }
  }

  trait Setup {
    val year2000       = 2000
    val year3000       = 3000
    val monthOfTheYear = 1
    val dayOfTheMonth  = 1

    val form: Form[LocalDate] = Form(AuthorityDateMapping.mapping(None, true))

    val validData: Gen[LocalDate] = datesBetween(
      min = LocalDate.of(year2000, monthOfTheYear, dayOfTheMonth),
      max = LocalDate.of(year3000, monthOfTheYear, dayOfTheMonth)
    )

    val invalidField: Gen[String] = Gen.alphaStr.suchThat(_.nonEmpty)

    val missingField: Gen[String] = Gen.const(emptyString)
  }

}
