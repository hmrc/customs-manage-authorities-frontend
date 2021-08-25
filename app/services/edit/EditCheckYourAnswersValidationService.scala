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

package services.edit

import com.google.inject.Inject
import models.domain.StandingAuthority
import models.{AuthorityEnd, AuthorityStart, ShowBalance, UserAnswers}
import pages.edit._
import services.DateTimeService

import scala.util.Try

class EditCheckYourAnswersValidationService @Inject()(dateTimeService: DateTimeService) {

  def validate(userAnswers: UserAnswers,
               accountId: String,
               authorityId: String,
               authorisedEori: String): Option[StandingAuthority] = Try {
    for {
      authorityStart <- userAnswers.get(EditAuthorityStartPage(accountId, authorityId))
      authorityEnd <- userAnswers.get(EditAuthorityEndPage(accountId, authorityId))
      authorisedFromDate <- if (authorityStart == AuthorityStart.Setdate) {
        userAnswers.get(EditAuthorityStartDatePage(accountId, authorityId))
      } else {
        Some(dateTimeService.localTime().toLocalDate)
      }
      authorityEndDate = if (authorityEnd == AuthorityEnd.Setdate) userAnswers.get(EditAuthorityEndDatePage(accountId, authorityId)) else None
      viewBalance <- userAnswers.get(EditShowBalancePage(accountId, authorityId))
      standingAuthority <- if (authorityEnd == AuthorityEnd.Setdate && authorityEndDate.isEmpty) {
        None
      } else {
        Some(StandingAuthority(
          authorisedEori,
          authorisedFromDate,
          authorityEndDate,
          viewBalance == ShowBalance.Yes
        ))
      }
    } yield standingAuthority
  }.recover { case _: IndexOutOfBoundsException => None }.toOption.flatten
}
