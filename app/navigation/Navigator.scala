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

package navigation

import controllers.routes
import models._
import pages._
import pages.add._
import pages.edit._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject()() {

  private val normalRoutes: Page => UserAnswers => Call = {
    case EoriNumberPage => _ => controllers.add.routes.AccountsController.onPageLoad(NormalMode)
    case AccountsPage => _ => controllers.add.routes.EoriDetailsCorrectController.onPageLoad(NormalMode)
    case EoriDetailsCorrectPage => eoriDetailsCorrectRoutes
    case AuthorityStartPage => authorityStartRoutes
    case AuthorityStartDatePage => _ => controllers.add.routes.ShowBalanceController.onPageLoad(NormalMode)
    case ShowBalancePage => _ => controllers.add.routes.AuthorityDetailsController.onPageLoad(NormalMode)
    case AuthorityDetailsPage => _ => controllers.add.routes.AuthorisedUserController.onPageLoad
    case AuthorisedUserPage => _ => controllers.add.routes.AddConfirmationController.onPageLoad
    case EditAuthorityStartPage(accountId: String, authorityId: String) => editAuthorityStartRoutes(_, accountId, authorityId)
    case EditAuthorityStartDatePage(accountId: String, authorityId: String) => editCheckYourAnswers(_, accountId, authorityId)
    case EditShowBalancePage(accountId: String, authorityId: String) => editCheckYourAnswers(_, accountId, authorityId)
    case EditAuthorisedUserPage(accountId: String, authorityId: String) => editCheckYourAnswers(_, accountId, authorityId)
    case EditCheckYourAnswersPage(accountId: String, authorityId: String) => _ => controllers.edit.routes.EditConfirmationController.onPageLoad(accountId, authorityId)
    case _ => _ => routes.IndexController.onPageLoad
  }

  private def editCheckYourAnswers(answers: UserAnswers, accountId: String, authorityId: String): Call = {
    answers.get(EditAuthorisedUserPage(accountId, authorityId)) match {
      case Some(_) => controllers.edit.routes.EditCheckYourAnswersController.onPageLoad(accountId, authorityId)
      case None => controllers.edit.routes.EditAuthorisedUserController.onPageLoad(accountId, authorityId)
    }
  }

  private def editAuthorityStartRoutes(answers: UserAnswers, accountId: String, authorityId: String): Call =
    answers.get(EditAuthorityStartPage(accountId, authorityId)) match {
      case Some(AuthorityStart.Today) => editCheckYourAnswers(answers, accountId, authorityId)
      case Some(AuthorityStart.Setdate) => controllers.edit.routes.EditAuthorityStartDateController.onPageLoad(accountId, authorityId)
      case _ => controllers.edit.routes.EditAuthorityStartController.onPageLoad(accountId, authorityId)
    }

  private val checkRoutes: Page => UserAnswers => Call = {
    case EoriNumberPage => _ => controllers.add.routes.AuthorityStartController.onPageLoad(NormalMode)
    case AuthorityStartPage => authorityStartCheckRoutes
    case AuthorityStartDatePage => _ => controllers.add.routes.AuthorisedUserController.onPageLoad
    case _ => _ => controllers.add.routes.AuthorisedUserController.onPageLoad
  }

  private def eoriDetailsCorrectRoutes(answers: UserAnswers): Call =
    answers.get(EoriDetailsCorrectPage) match {
      case Some(EoriDetailsCorrect.Yes) => controllers.add.routes.AuthorityStartController.onPageLoad(NormalMode)
      case Some(EoriDetailsCorrect.No) => controllers.add.routes.EoriNumberController.onPageLoad(NormalMode)
      case _ => controllers.add.routes.EoriDetailsCorrectController.onPageLoad(NormalMode)
    }

  private def authorityStartRoutes(answers: UserAnswers): Call =
    answers.get(AuthorityStartPage) match {
      case Some(AuthorityStart.Today) => controllers.add.routes.ShowBalanceController.onPageLoad(NormalMode)
      case Some(AuthorityStart.Setdate) => controllers.add.routes.AuthorityStartDateController.onPageLoad(NormalMode)
      case _ => controllers.add.routes.AuthorityStartController.onPageLoad(NormalMode)
    }

  private def authorityStartCheckRoutes(answers: UserAnswers): Call =
    answers.get(AuthorityStartPage) match {
      case Some(AuthorityStart.Today) => controllers.add.routes.AuthorisedUserController.onPageLoad
      case Some(AuthorityStart.Setdate) => controllers.add.routes.AuthorityStartDateController.onPageLoad(CheckMode)
      case _ => controllers.add.routes.AuthorityStartController.onPageLoad(CheckMode)
    }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRoutes(page)(userAnswers)
  }

  def backLinkRoute(mode: Mode, call: Call): Call = mode match {
    case NormalMode => call
    case CheckMode => controllers.add.routes.AuthorisedUserController.onPageLoad
  }

  def backLinkRouteForShowBalancePage(mode: Mode, answers: UserAnswers): Call = {
    (mode, answers.get(AuthorityStartPage)) match {
      case (NormalMode, Some(AuthorityStart.Today)) => controllers.add.routes.AuthorityStartController.onPageLoad(mode)
      case (NormalMode, Some(AuthorityStart.Setdate)) => controllers.add.routes.AuthorityStartDateController.onPageLoad(mode)
      case (NormalMode, None) => controllers.add.routes.AuthorityStartController.onPageLoad(mode)
      case (CheckMode, _) => controllers.add.routes.AuthorisedUserController.onPageLoad
    }
  }

  def backLinkRouteForEORINUmberPage(mode: Mode): Call = {
    mode match {
      case CheckMode => controllers.add.routes.AuthorisedUserController.onPageLoad
      case NormalMode => controllers.routes.ManageAuthoritiesController.onPageLoad
    }
  }
}

