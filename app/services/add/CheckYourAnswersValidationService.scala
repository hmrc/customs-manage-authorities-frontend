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
import models.domain.{AuthorisedUser, CDSAccount, StandingAuthority}
import models.requests.Accounts
import models.{AuthorityEnd, AuthorityStart, ShowBalance, UserAnswers}
import pages.add._
import services.DateTimeService
import utils.Constants.{CASH_ACCOUNT_TYPE, DUTY_DEFERMENT_ACCOUNT_TYPE, GENERAL_GUARANTEE_ACCOUNT_TYPE}

import scala.util.Try

class CheckYourAnswersValidationService @Inject()(dateTimeService: DateTimeService) {

  def validate(userAnswers: UserAnswers): Option[(Accounts, StandingAuthority, AuthorisedUser)] = Try {
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
      authorisedUser <- userAnswers.get(AuthorityDetailsPage)
      standingAuthority <- if (authorityEnd == AuthorityEnd.Setdate && authorityEndDate.isEmpty) {
        None
      } else {
        Some(StandingAuthority(
          authorisedEori.eori,
          authorisedFromDate,
          authorityEndDate,
          viewBalance == ShowBalance.Yes
        ))
      }
    } yield (accounts, standingAuthority, authorisedUser)
  }.recover { case _: IndexOutOfBoundsException => None }.toOption.flatten

  private def extractAccounts(selected: List[CDSAccount]): Accounts = {
    Accounts(
      selected.find(_.accountType == CASH_ACCOUNT_TYPE).map(_.number),
      selected.filter(_.accountType == DUTY_DEFERMENT_ACCOUNT_TYPE).map(_.number),
      selected.find(_.accountType == GENERAL_GUARANTEE_ACCOUNT_TYPE).map(_.number)
    )
  }

}
