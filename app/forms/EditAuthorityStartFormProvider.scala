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

package forms

import forms.mappings.Mappings
import models.AuthorityStart
import play.api.data.Form
import play.api.i18n.Messages
import utils.DateUtils

import java.time.LocalDate
import javax.inject.Inject

class EditAuthorityStartFormProvider @Inject() extends Mappings with DateUtils {

  def apply(maybeEndDate: Option[LocalDate])(implicit messages: Messages): Form[AuthorityStart] =
    Form(
      "value" -> enumerable[AuthorityStart](requiredKey = "authorityStart.error.required")
        .verifying(
          maybeMinDate(
            maybeEndDate,
            errorKey = "authorityStartDate.error.maximum",
            dateAsDayMonthAndYear(maybeEndDate.getOrElse(LocalDate.MAX))
          )
        )
    )
}
