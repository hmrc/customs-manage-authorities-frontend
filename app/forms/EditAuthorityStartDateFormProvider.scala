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

import forms.mappings.Mappings
import play.api.data.Form
import play.api.i18n.Messages
import services.DateTimeService
import utils.DateUtils

import java.time.LocalDate
import javax.inject.Inject

class EditAuthorityStartDateFormProvider @Inject()(dateTimeService: DateTimeService) extends Mappings with DateUtils {

  def apply(maybeEndDate: Option[LocalDate])(implicit messages: Messages): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey = "authorityStartDate.error.invalid",
        allRequiredKey = "authorityStartDate.error.required.all",
        twoRequiredKey = "authorityStartDate.error.required.two",
        requiredKey = "authorityStartDate.error.required"
      ).verifying(minDate(dateTimeService.localTime().toLocalDate,
        "authorityStartDate.error.minimum", "authorityStartDate.error.year.length"))
       .verifying(maybeMaxDate(maybeEndDate, "authorityStartDate.error.maximum", dateAsDayMonthAndYear(maybeEndDate.getOrElse(LocalDate.MAX))))
    )
}
