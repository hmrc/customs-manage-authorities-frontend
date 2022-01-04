/*
 * Copyright 2022 HM Revenue & Customs
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
import models.domain.AuthorisedUser
import play.api.data.Form
import play.api.data.Forms.{checked, mapping}

import javax.inject.Inject

class AuthorisedUserFormProviderWithConsent @Inject() extends Mappings {

  def apply(): Form[AuthorisedUser] =
    Form(
      mapping(
      "fullName" -> text("authorisedUser.error.fullName.required")
        .verifying(maxLength(255, "authorisedUser.error.fullName.length"))
        .verifying(regexp(textFieldRegex, "authorisedUser.error.fullName.malicious")),
      "jobRole" -> text("authorisedUser.error.jobRole.required")
        .verifying(maxLength(255, "authorisedUser.error.jobRole.length"))
        .verifying(regexp(textFieldRegex, "authorisedUser.error.jobRole.malicious")),
        "confirmation" -> checked("authorisedUser.error.confirmation.required")
      )((name, role, _) => AuthorisedUser.apply(name, role))(user => Some(user.userName, user.userRole, false))
    )
}
