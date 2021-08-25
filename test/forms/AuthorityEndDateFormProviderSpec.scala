/*
 * Copyright 2021 HM Revenue & Customs
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

package forms

import base.SpecBase
import forms.behaviours.DateBehaviours
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.data.FormError
import services.DateTimeService
import viewmodels.ManageAuthoritiesViewModel.dateAsDayMonthAndYear
import java.time.{LocalDate, LocalDateTime}

class AuthorityEndDateFormProviderSpec extends DateBehaviours {

  val specBase = new SpecBase{}
  implicit val messages = specBase.messagesApi.preferred(specBase.fakeRequest())

  val mockDateTimeService = mock[DateTimeService]
  when(mockDateTimeService.localTime()).thenReturn(LocalDateTime.now())

  ".value starting today" should {

    val form = new AuthorityEndDateFormProvider(mockDateTimeService)(LocalDate.now())

    val validData = datesBetween(
      min = LocalDate.now(),
      max = LocalDate.now().plusYears(1)
    )

    "reject a date before the start date" in {
      val date = LocalDate.now().minusDays(1)

      val data = Map(
        s"value.day" -> date.getDayOfMonth.toString,
        s"value.month" -> date.getMonthValue.toString,
        s"value.year" -> date.getYear.toString
      )

      form.bind(data).errors shouldBe Seq(FormError("value", List("authorityEndDate.error.minimum"), Seq(dateAsDayMonthAndYear(LocalDate.now()))))
    }

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "authorityEndDate.error.required.all")
  }

  ".value starting in the future" should {

    val startDate = LocalDate.now().plusDays(5)

    val form = new AuthorityEndDateFormProvider(mockDateTimeService)(startDate)

    val validData = datesBetween(
      min = startDate,
      max = LocalDate.now().plusYears(1)
    )

    "reject a date before the start date" in {
      val dateBefore = startDate.minusDays(1)

      val data = Map(
        s"value.day" -> dateBefore.getDayOfMonth.toString,
        s"value.month" -> dateBefore.getMonthValue.toString,
        s"value.year" -> dateBefore.getYear.toString
      )

      form.bind(data).errors shouldBe Seq(FormError("value", List("authorityEndDate.error.minimum"), Seq(dateAsDayMonthAndYear(startDate))))
    }

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "authorityEndDate.error.required.all")
  }
}
