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

package forms

import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.i18n.Messages
import play.api.test.Helpers
import forms.behaviours.DateBehaviours
import services.DateTimeService

import java.time._

class EditAuthorityStartDateFormProviderSpec extends DateBehaviours {

  implicit val messages: Messages = Helpers.stubMessages()
  val mockDateTimeService = mock[DateTimeService]
  when(mockDateTimeService.localTime()).thenReturn(LocalDateTime.now())

  val form = new EditAuthorityStartDateFormProvider(mockDateTimeService)(None)

  ".value" should {

    val validData = datesBetween(
      min = LocalDate.now(ZoneOffset.UTC),
      max = LocalDate.now(ZoneOffset.UTC).plusYears(1)
    )

    behave like dateField(form, "value", validData)
    behave like mandatoryDateField(form, "value.day", "authorityStartDate.error.required.all")
  }
}
