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

package services.add

import com.google.inject.Inject
import models.UserAnswers
import models.requests.AddAuthorityRequest

class AddAuthorityValidationService @Inject() (cyaValidationService: CheckYourAnswersValidationService) {

  def validate(userAnswers: UserAnswers, ownerEori: String): Option[AddAuthorityRequest] =
    for {
      (accounts, standingAuthority, authorisedUser) <- cyaValidationService.validate(userAnswers)
    } yield AddAuthorityRequest(
      accounts,
      standingAuthority,
      authorisedUser,
      ownerEori = ownerEori
    )
}
