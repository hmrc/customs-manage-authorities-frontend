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

package services

import models.domain.{AccountWithAuthoritiesWithId, StandingAuthority}
import models.{AuthorityStart, ShowBalance, UserAnswers}
import pages.edit._
import play.api.i18n.Messages
import repositories.SessionRepository
import viewmodels.CheckYourAnswersEditHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EditSessionService @Inject()(sessionRepository: SessionRepository,
                                   dateTimeService: DateTimeService
                                  )(implicit executionContext: ExecutionContext) {

  def resetUserAnswers(accountId: String,
                       authorityId: String,
                       userAnswers: UserAnswers,
                       authority: StandingAuthority,
                       account: AccountWithAuthoritiesWithId
                         )(implicit messages: Messages): Future[CheckYourAnswersEditHelper] = {
    val newUserAnswers = UserAnswers(userAnswers.id)
    for {
      populatedStartDatePage <- populateStartDatePage(newUserAnswers, accountId, authorityId, authority)
      populatedStartPage <- populateStartPage(populatedStartDatePage, accountId, authorityId, authority)
      updatedAnswers <- populateShowBalance(populatedStartPage, accountId, authorityId, authority)
      _ <- sessionRepository.set(updatedAnswers)
    } yield new CheckYourAnswersEditHelper(updatedAnswers, accountId, authorityId, dateTimeService, authority, account)
  }

  private def populateStartPage(userAnswers: UserAnswers, accountId: String, authorityId: String, authority: StandingAuthority): Future[UserAnswers] = {
    val authorityStart = if (authority.authorisedFromDate == dateTimeService.localDate()) AuthorityStart.Today else AuthorityStart.Setdate
    Future.fromTry(userAnswers.set(EditAuthorityStartPage(accountId, authorityId), authorityStart)(AuthorityStart.writes))
  }

  private def populateStartDatePage(userAnswers: UserAnswers, accountId: String, authorityId: String, authority: StandingAuthority): Future[UserAnswers] = {
    Future.fromTry(userAnswers.set(EditAuthorityStartDatePage(accountId, authorityId), authority.authorisedFromDate))
  }

  private def populateShowBalance(userAnswers: UserAnswers, accountId: String, authorityId: String, authority: StandingAuthority): Future[UserAnswers] = {
    Future.fromTry(userAnswers.set(EditShowBalancePage(accountId, authorityId), ShowBalance.fromBoolean(authority.viewBalance))(ShowBalance.writes))
  }
}
