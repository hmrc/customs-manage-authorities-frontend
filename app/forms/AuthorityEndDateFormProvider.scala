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

import com.google.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import play.api.i18n.Messages
import services.DateTimeService
import utils.DateUtils

import java.time.LocalDate

class AuthorityEndDateFormProvider @Inject()(dateTimeService: DateTimeService) extends Mappings with DateUtils {

  def apply(startDate: LocalDate)(implicit messages: Messages): Form[LocalDate] = {
    val minimumDate = latestOf(startDate, dateTimeService.localTime().toLocalDate)
    Form(
      "value" -> localDate(
        invalidKey     = "authorityEndDate.error.invalid",
        allRequiredKey = "authorityEndDate.error.required.all",
        twoRequiredKey = "authorityEndDate.error.required.two",
        requiredKey    = "authorityEndDate.error.required"
      ).verifying(minDate(minimumDate, "authorityEndDate.error.minimum", dateAsDayMonthAndYear(minimumDate)))
    )
  }
}
