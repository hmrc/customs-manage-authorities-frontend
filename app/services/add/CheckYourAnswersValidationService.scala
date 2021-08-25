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

package services.add

import com.google.inject.Inject
import models.domain.{CDSAccount, StandingAuthority}
import models.requests.Accounts
import models.{AuthorityEnd, AuthorityStart, ShowBalance, UserAnswers}
import pages.add._
import services.DateTimeService

import scala.util.Try

class CheckYourAnswersValidationService @Inject()(dateTimeService: DateTimeService) {

  def validate(userAnswers: UserAnswers): Option[(Accounts, StandingAuthority)] = Try {
    for {
      selectedAccounts <- userAnswers.get(AccountsPage)
      accounts = extractAccounts(selectedAccounts)
      authorisedEori <- userAnswers.get(EoriNumberPage)
      authorityStart <- userAnswers.get(AuthorityStartPage)
      authorityEnd <- userAnswers.get(AuthorityEndPage)
      authorisedFromDate <- if (authorityStart == AuthorityStart.Setdate) {
        userAnswers.get(AuthorityStartDatePage)
      } else {
        Some(dateTimeService.localTime().toLocalDate)
      }
      authorityEndDate = if (authorityEnd == AuthorityEnd.Setdate) userAnswers.get(AuthorityEndDatePage) else None
      viewBalance <- userAnswers.get(ShowBalancePage)
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
    } yield (accounts, standingAuthority)
  }.recover { case _: IndexOutOfBoundsException => None }.toOption.flatten

  private def extractAccounts(selected: List[CDSAccount]): Accounts = {
    Accounts(
      selected.find(_.accountType == "cash").map(_.number),
      selected.filter(_.accountType == "dutyDeferment").map(_.number),
      selected.find(_.accountType == "generalGuarantee").map(_.number)
    )
  }

}
