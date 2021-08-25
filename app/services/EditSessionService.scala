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

package services

import models.domain.{AccountWithAuthoritiesWithId, StandingAuthority}
import models.{AuthorityEnd, AuthorityStart, ShowBalance, UserAnswers}
import pages.edit._
import play.api.i18n.Messages
import repositories.SessionRepository
import viewmodels.CheckYourAnswersEditHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EditSessionService @Inject()(sessionRepository: SessionRepository,
                                   dateTimeService: DateTimeService
                                  )(implicit executionContext: ExecutionContext) {

  def resetStartAnswers(userAnswers: UserAnswers, accountId: String, authorityId: String, authority: StandingAuthority): Future[UserAnswers] = {
    for {
      populatedStart <- populateStartPage(userAnswers, accountId, authorityId, authority)
      updatedUserAnswers <- populateStartDatePage(populatedStart, accountId, authorityId, authority)
      _ <- sessionRepository.set(updatedUserAnswers)
    } yield updatedUserAnswers
  }

  def resetEndAnswers(userAnswers: UserAnswers, accountId: String, authorityId: String, authority: StandingAuthority): Future[UserAnswers] = {
    for {
      populatedEnd <- populateEndPage(userAnswers, accountId, authorityId, authority)
      updatedUserAnswers <- populateEndDatePage(populatedEnd, accountId, authorityId, authority)
      _ <- sessionRepository.set(updatedUserAnswers)
    } yield updatedUserAnswers
  }

  def populateUserAnswers(accountId: String,
                          authorityId: String,
                          userAnswers: UserAnswers,
                          authority: StandingAuthority,
                          account: AccountWithAuthoritiesWithId
                         )(implicit messages: Messages): Future[CheckYourAnswersEditHelper] = {
    val newUserAnswers = UserAnswers(userAnswers.id)
    for {
      populatedStartDatePage <- userAnswers.get(EditAuthorityStartDatePage(accountId, authorityId)).fold(
        populateStartDatePage(newUserAnswers, accountId, authorityId, authority)
      )(v => Future.fromTry(newUserAnswers.set(EditAuthorityStartDatePage(accountId, authorityId), v)))

      populatedStartPage <- userAnswers.get(EditAuthorityStartPage(accountId, authorityId)).fold {
        populateStartPage(populatedStartDatePage, accountId, authorityId, authority)
      }(v => Future.fromTry(populatedStartDatePage.set(EditAuthorityStartPage(accountId, authorityId), v)(AuthorityStart.writes)))

      populatedEndDatePage <- userAnswers.get(EditAuthorityEndDatePage(accountId, authorityId)).fold(
        populateEndDatePage(populatedStartPage, accountId, authorityId, authority)
      )(v => Future.fromTry(populatedStartPage.set(EditAuthorityEndDatePage(accountId, authorityId), v)))

      populatedEndPage <- userAnswers.get(EditAuthorityEndPage(accountId, authorityId)).fold(
        populateEndPage(populatedEndDatePage, accountId, authorityId, authority)
      )(v => Future.fromTry(populatedEndDatePage.set(EditAuthorityEndPage(accountId, authorityId), v)(AuthorityEnd.writes)))

      updatedAnswers <- userAnswers.get(EditShowBalancePage(accountId, authorityId)).fold(
        populateShowBalance(populatedEndPage, accountId, authorityId, authority)
      )(v => Future.fromTry(populatedEndPage.set(EditShowBalancePage(accountId, authorityId), v)(ShowBalance.writes)))

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

  private def populateEndDatePage(userAnswers: UserAnswers, accountId: String, authorityId: String, authority: StandingAuthority): Future[UserAnswers] = {
    authority.authorisedToDate.map(
      value => Future.fromTry(userAnswers.set(EditAuthorityEndDatePage(accountId, authorityId), value))
    ).getOrElse(Future.successful(userAnswers))
  }

  private def populateEndPage(userAnswers: UserAnswers, accountId: String, authorityId: String, authority: StandingAuthority): Future[UserAnswers] = {
    authority.authorisedToDate match {
      case Some(_) => Future.fromTry(userAnswers.set(EditAuthorityEndPage(accountId, authorityId), AuthorityEnd.Setdate)(AuthorityEnd.writes))
      case None => Future.fromTry(userAnswers.set(EditAuthorityEndPage(accountId, authorityId), AuthorityEnd.Indefinite)(AuthorityEnd.writes))
    }
  }

  private def populateShowBalance(userAnswers: UserAnswers, accountId: String, authorityId: String, authority: StandingAuthority): Future[UserAnswers] = {
    Future.fromTry(userAnswers.set(EditShowBalancePage(accountId, authorityId), ShowBalance.fromBoolean(authority.viewBalance))(ShowBalance.writes))
  }

}
