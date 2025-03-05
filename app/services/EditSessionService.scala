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

package services

import connectors.CustomsDataStoreConnector
import models.domain.{AccountWithAuthoritiesWithId, StandingAuthority}
import models.{AuthorityEnd, AuthorityStart, ShowBalance, UserAnswers}
import pages.edit._
import play.api.i18n.Messages
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.CheckYourAnswersEditHelper

import javax.inject.Inject
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class EditSessionService @Inject() (sessionRepository: SessionRepository, dateTimeService: DateTimeService)(implicit
  executionContext: ExecutionContext
) {

  def resetUserAnswers(
    accountId: String,
    authorityId: String,
    userAnswers: UserAnswers,
    authority: StandingAuthority,
    account: AccountWithAuthoritiesWithId,
    dataStore: CustomsDataStoreConnector
  )(implicit messages: Messages, hc: HeaderCarrier): Future[CheckYourAnswersEditHelper] = {

    val newUserAnswers = UserAnswers(userAnswers.id)
    val companyName    = Await.result(dataStore.getCompanyName, Duration.Inf)

    for {
      populatedStartDatePage <- populateStartDatePage(newUserAnswers, accountId, authorityId, authority)
      populatedStartPage     <- populateStartPage(populatedStartDatePage, accountId, authorityId, authority)
      populatedEndDatePage   <- populateEndDatePage(populatedStartPage, accountId, authorityId, authority)
      populatedEndPage       <- populateEndPage(populatedEndDatePage, accountId, authorityId, authority)
      updatedAnswers         <- populateShowBalance(populatedEndPage, accountId, authorityId, authority)
      _                      <- sessionRepository.set(updatedAnswers)
    } yield new CheckYourAnswersEditHelper(
      updatedAnswers,
      accountId,
      authorityId,
      dateTimeService,
      authority,
      account,
      companyName
    )
  }

  private def populateStartPage(
    userAnswers: UserAnswers,
    accountId: String,
    authorityId: String,
    authority: StandingAuthority
  ): Future[UserAnswers] = {
    val authorityStart =
      if (authority.authorisedFromDate == dateTimeService.localDate()) AuthorityStart.Today else AuthorityStart.Setdate

    Future.fromTry(
      userAnswers.set(EditAuthorityStartPage(accountId, authorityId), authorityStart)(AuthorityStart.writes)
    )
  }

  private def populateStartDatePage(
    userAnswers: UserAnswers,
    accountId: String,
    authorityId: String,
    authority: StandingAuthority
  ): Future[UserAnswers] =
    Future.fromTry(userAnswers.set(EditAuthorityStartDatePage(accountId, authorityId), authority.authorisedFromDate))

  private def populateEndDatePage(
    userAnswers: UserAnswers,
    accountId: String,
    authorityId: String,
    authority: StandingAuthority
  ): Future[UserAnswers] =
    authority.authorisedToDate
      .map(value => Future.fromTry(userAnswers.set(EditAuthorityEndDatePage(accountId, authorityId), value)))
      .getOrElse(Future.successful(userAnswers))

  private def populateEndPage(
    userAnswers: UserAnswers,
    accountId: String,
    authorityId: String,
    authority: StandingAuthority
  ): Future[UserAnswers] =
    authority.authorisedToDate match {
      case Some(_) =>
        Future.fromTry(
          userAnswers.set(EditAuthorityEndPage(accountId, authorityId), AuthorityEnd.Setdate)(AuthorityEnd.writes)
        )

      case None =>
        Future.fromTry(
          userAnswers.set(EditAuthorityEndPage(accountId, authorityId), AuthorityEnd.Indefinite)(AuthorityEnd.writes)
        )
    }

  private def populateShowBalance(
    userAnswers: UserAnswers,
    accountId: String,
    authorityId: String,
    authority: StandingAuthority
  ): Future[UserAnswers] =
    Future.fromTry(
      userAnswers.set(EditShowBalancePage(accountId, authorityId), ShowBalance.fromBoolean(authority.viewBalance))(
        ShowBalance.writes
      )
    )

}
