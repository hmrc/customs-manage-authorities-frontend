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

import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.data.FormError
import base.SpecBase

import java.time.LocalDate

class LocalDateFormatterSpec extends SpecBase {
    "bind" must {
        "return the correct LocalDate when the supplied data is valid" in new SetUp {
            localDateFormatter.bind(key, bindDataValid) shouldBe Right(LocalDate.of(2022, 10, 12))
        }

        "return the correct FormError with keys when the supplied data is invalid" in new SetUp {
            localDateFormatter.bind(key, bindDataEmptyDate) shouldBe
                Left(Seq(FormError("start.day", List(allRequiredMsgKey), List())))

            localDateFormatter.bind(key, bindDataDateWithEmptyMonth) shouldBe
                Left(Seq(FormError("start.month", List(requiredMsgKey), List("month"))))

            localDateFormatter.bind(key, bindDataDateWithEmptyYear) shouldBe
                Left(Seq(FormError("start.year", List(requiredMsgKey), List("year"))))

            localDateFormatter.bind(key, bindDataInValidDate) shouldBe
                Left(Seq(FormError("start.day", List(invalidMsgKey), List())))

            localDateFormatter.bind(key, bindDataInValidMonth) shouldBe
                Left(Seq(FormError("start.month", List(invalidMsgKey), List())))

            localDateFormatter.bind(key, bindDataInValidYear) shouldBe
                Left(Seq(FormError("start.year", List(invalidMsgKey), List())))
        }
    }

    "updateFormErrorKeys" must {
        "append the day in the existing key when day is incorrect or all(day, month and year) are incorrect" in new SetUp { 
            localDateFormatter.updateFormErrorKeys(key, 32, 12, 2023) shouldBe s"$key.day"
        }

        "append the month in the existing key when month is incorrect" in new SetUp {
            localDateFormatter.updateFormErrorKeys(key, 12, 14, 2023) shouldBe s"$key.month"
        }

        "append the year in the existing key when year is incorrect" in new SetUp {
            localDateFormatter.updateFormErrorKeys(key, 12, 12, -2022) shouldBe s"$key.year"
        }
    }

    "formErrorKeysInCaseOfEmptyOrNonNumericValues" must {
        "return key.day as updated key when day value is empty" in new SetUp { 
            val formDataWithEmptyDay: Map[String, String] = Map(s"$key.day" -> "", s"$key.month" -> "10", s"$key.year" -> "2021")

            localDateFormatter.formErrorKeysInCaseOfEmptyOrNonNumericValues(
                key, formDataWithEmptyDay) shouldBe s"$key.day"
        }

        "return key.month as updated key when month value is empty" in new SetUp { 
            val formDataWithEmptyMonth: Map[String, String] = Map(s"$key.day" -> "10", s"$key.month" -> "", s"$key.year" -> "2021")

            localDateFormatter.formErrorKeysInCaseOfEmptyOrNonNumericValues(
                key, formDataWithEmptyMonth) shouldBe s"$key.month"
        }

        "return key.year as updated key when year value is empty" in new SetUp { 
            val formDataWithEmptyYear: Map[String, String] = Map(s"$key.day" -> "10", s"$key.month" -> "10", s"$key.year" -> "")

            localDateFormatter.formErrorKeysInCaseOfEmptyOrNonNumericValues(
                key, formDataWithEmptyYear) shouldBe s"$key.year"
        }

        "return key.day as updated key when all date fields are empty" in new SetUp { 
            val formDataWithEmptyDate: Map[String, String] = Map(s"$key.day" -> "", s"$key.month" -> "", s"$key.year" -> "")

            localDateFormatter.formErrorKeysInCaseOfEmptyOrNonNumericValues(
                key, formDataWithEmptyDate) shouldBe s"$key.day"
        }

        "return key.day as updated key when day value is not numeric" in new SetUp { 
            val formDataWithNonNumericDay: Map[String, String] = Map(s"$key.day" -> "se", s"$key.month" -> "10", s"$key.year" -> "2021")

            localDateFormatter.formErrorKeysInCaseOfEmptyOrNonNumericValues(
                key, formDataWithNonNumericDay) shouldBe s"$key.day"
        }

        "return key.month as updated key when month value is not numeric" in new SetUp { 
            val formDataWithNonNumericMonth: Map[String, String] = Map(s"$key.day" -> "10", s"$key.month" -> "test", s"$key.year" -> "2021")

            localDateFormatter.formErrorKeysInCaseOfEmptyOrNonNumericValues(
                key, formDataWithNonNumericMonth) shouldBe s"$key.month"
        }

        "return key.year as updated key when year value is not numeric" in new SetUp { 
            val formDataWithNonNumericYear: Map[String, String] = Map(s"$key.day" -> "10", s"$key.month" -> "10", s"$key.year" -> "et")

            localDateFormatter.formErrorKeysInCaseOfEmptyOrNonNumericValues(
                key, formDataWithNonNumericYear) shouldBe s"$key.year"
        }
    }
}

trait SetUp {
    val key = "start"
    val invalidMsgKey = "authorityStartDate.error.invalid"
    val allRequiredMsgKey = "authorityStartDate.error.required.all"
    val twoRequiredMsgKey = "authorityStartDate.error.required.two"
    val requiredMsgKey =  "authorityStartDate.error.required"
    val bindDataValid: Map[String, String] = Map("start.day" -> "12", "start.month" -> "10", "start.year" -> "2022")
    val bindDataEmptyDate: Map[String, String] = Map("start.day" -> "", "start.month" -> "", "start.year" -> "")
    val bindDataDateWithEmptyMonth: Map[String, String] = Map("start.day" -> "10", "start.month" -> "", "start.year" -> "2022")
    val bindDataDateWithEmptyYear: Map[String, String] = Map("start.day" -> "10", "start.month" -> "10", "start.year" -> "")
    val bindDataInValidDate: Map[String, String] = Map("start.day" -> "34", "start.month" -> "14", "start.year" -> "2023")
    val bindDataInValidMonth: Map[String, String] = Map("start.day" -> "10", "start.month" -> "14", "start.year" -> "2022")
    val bindDataInValidYear: Map[String, String] = Map("start.day" -> "10", "start.month" -> "10", "start.year" -> "-")
    val localDateFormatter = new LocalDateFormatter(
        invalidMsgKey, 
        allRequiredMsgKey,
        twoRequiredMsgKey,
        requiredMsgKey,
        Seq()
    )
}
