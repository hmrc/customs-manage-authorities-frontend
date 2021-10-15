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

package navigation

import base.SpecBase
import controllers.routes
import models.AuthorityEnd.Indefinite
import models.AuthorityStart.{Setdate, Today}
import models._
import models.domain.{AccountStatusOpen, AccountWithAuthorities, AuthorisedUser, CdsCashAccount, StandingAuthority}
import models.requests.Accounts
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages._
import pages.add._
import pages.edit.{EditAuthorisedUserPage, EditAuthorityEndDatePage, EditAuthorityEndPage, EditAuthorityStartDatePage, EditAuthorityStartPage, EditShowBalancePage}
import services.add.CheckYourAnswersValidationService

import java.time.LocalDate

class NavigatorSpec extends SpecBase with MockitoSugar {

  val navigator = new Navigator()
  val accounts = Accounts(Some(AccountWithAuthorities(CdsCashAccount, "12345", Some(AccountStatusOpen), Seq.empty)), Seq.empty, None)
  val standingAuthority = StandingAuthority("GB123456789012", LocalDate.now(), None, viewBalance = true)
  val mockValidator = mock[CheckYourAnswersValidationService]
  when(mockValidator.validate(any())).thenReturn(Some((accounts, standingAuthority)))

  "Navigator" when {

    "in Normal mode" must {

      "go to Index from a page that doesn't exist in the route map" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, emptyUserAnswers) mustBe routes.IndexController.onPageLoad()
      }

      "go from Accounts to EoriNumber" in {
        navigator.nextPage(AccountsPage, NormalMode, emptyUserAnswers) mustBe controllers.add.routes.AuthorityStartController.onPageLoad(NormalMode)
      }

      "go from AuthorisedUserPage to AddConfirmationPage" in {
        navigator.nextPage(AuthorisedUserPage, NormalMode, emptyUserAnswers) mustBe controllers.add.routes.AddConfirmationController.onPageLoad()
      }

      "go from EditAuthorityStartDatePage to EditCheckYourAnswers when authorised user populated" in {
        val userAnswers = emptyUserAnswers
          .set(EditAuthorisedUserPage("a", "b"), AuthorisedUser("test", "test")).get

        navigator.nextPage(EditAuthorityStartDatePage("a", "b"), NormalMode, userAnswers) mustBe
          controllers.edit.routes.EditCheckYourAnswersController.onPageLoad("a", "b")
      }

      "go from EditAuthorityEndDatePage to EditCheckYourAnswers when authorised user populated" in {
        val userAnswers = emptyUserAnswers
          .set(EditAuthorisedUserPage("a", "b"), AuthorisedUser("test", "test")).get

        navigator.nextPage(EditAuthorityEndDatePage("a", "b"), NormalMode, userAnswers) mustBe
          controllers.edit.routes.EditCheckYourAnswersController.onPageLoad("a", "b")
      }

      "go from EditShowBalancePage to EditCheckYourAnswers when authorised user populated" in {
        val userAnswers = emptyUserAnswers
          .set(EditAuthorisedUserPage("a", "b"), AuthorisedUser("test", "test")).get

        navigator.nextPage(EditShowBalancePage("a", "b"), NormalMode, userAnswers) mustBe
          controllers.edit.routes.EditCheckYourAnswersController.onPageLoad("a", "b")
      }

      "go from EditAuthorisedUserPage to EditCheckYourAnswers when authorised user populated" in {
        val userAnswers = emptyUserAnswers
          .set(EditAuthorisedUserPage("a", "b"), AuthorisedUser("test", "test")).get

        navigator.nextPage(EditAuthorisedUserPage("a", "b"), NormalMode, userAnswers) mustBe
          controllers.edit.routes.EditCheckYourAnswersController.onPageLoad("a", "b")
      }

      "go from EditAuthorityEndPage to EditCheckYourAnswers when authorised user populated and date set to until further notice" in {
        val userAnswers = emptyUserAnswers
          .set(EditAuthorityEndPage("a", "b"), Indefinite).get
          .set(EditAuthorisedUserPage("a", "b"), AuthorisedUser("test", "test")).get

        navigator.nextPage(EditAuthorityEndPage("a", "b"), NormalMode, userAnswers) mustBe
          controllers.edit.routes.EditCheckYourAnswersController.onPageLoad("a", "b")
      }

      "go from EditAuthorityEndPage to EditAuthorityEndDate when date set to Setdate" in {
        val userAnswers = emptyUserAnswers
          .set(EditAuthorityEndPage("a", "b"), AuthorityEnd.Setdate).get

        navigator.nextPage(EditAuthorityEndPage("a", "b"), NormalMode, userAnswers) mustBe
          controllers.edit.routes.EditAuthorityEndDateController.onPageLoad("a", "b")
      }

      "go from EditAuthorityEndPage to EditAuthorityEndPage when no user answers available" in {
        navigator.nextPage(EditAuthorityEndPage("a", "b"), NormalMode, emptyUserAnswers) mustBe
          controllers.edit.routes.EditAuthorityEndController.onPageLoad("a", "b")
      }

      "go from EditAuthorityStartPage to EditCheckYourAnswers when authorised user populated and date set to today" in {
        val userAnswers = emptyUserAnswers
          .set(EditAuthorityStartPage("a", "b"), Today).get
          .set(EditAuthorisedUserPage("a", "b"), AuthorisedUser("test", "test")).get

        navigator.nextPage(EditAuthorityStartPage("a", "b"), NormalMode, userAnswers) mustBe
          controllers.edit.routes.EditCheckYourAnswersController.onPageLoad("a", "b")
      }

      "go from EditAuthorityStartPage to EditAuthorisedUserPage when date set to today and no authorised user" in {
        val userAnswers = emptyUserAnswers
          .set(EditAuthorityStartPage("a", "b"), Today).get

        navigator.nextPage(EditAuthorityStartPage("a", "b"), NormalMode, userAnswers) mustBe
          controllers.edit.routes.EditAuthorisedUserController.onPageLoad("a", "b")
      }

      "go from EditAuthorityStartPage to EditAuthorityStartDate when date set to SetDate" in {
        val userAnswers = emptyUserAnswers
          .set(EditAuthorityStartPage("a", "b"), Setdate).get

        navigator.nextPage(EditAuthorityStartPage("a", "b"), NormalMode, userAnswers) mustBe
          controllers.edit.routes.EditAuthorityStartDateController.onPageLoad("a", "b")
      }

      "go from EditAuthorityStartPage to EditAuthorityStartPage when date not populated" in {
        navigator.nextPage(EditAuthorityStartPage("a", "b"), NormalMode, emptyUserAnswers) mustBe
          controllers.edit.routes.EditAuthorityStartController.onPageLoad("a", "b")
      }


      "go from EoriNumber to AuthorityStart" in {
        navigator.nextPage(EoriNumberPage, NormalMode, emptyUserAnswers) mustBe controllers.add.routes.AccountsController.onPageLoad(NormalMode)
      }

      "go from AuthorityStart to AuthorityStartDate when 'set date' is chosen" in {
        val userAnswers = emptyUserAnswers.set(AuthorityStartPage, AuthorityStart.Setdate)(AuthorityStart.writes).success.value
        navigator.nextPage(AuthorityStartPage, NormalMode, userAnswers) mustBe controllers.add.routes.AuthorityStartDateController.onPageLoad(NormalMode)
      }

      "go from AuthorityStart to AuthorityEnd when 'today' is chosen" in {
        val userAnswers = emptyUserAnswers.set(AuthorityStartPage, AuthorityStart.Today)(AuthorityStart.writes).success.value
        navigator.nextPage(AuthorityStartPage, NormalMode, userAnswers) mustBe controllers.add.routes.AuthorityEndController.onPageLoad(NormalMode)
      }

      "go from AuthorityStartDate to AuthorityEnd" in {
        navigator.nextPage(AuthorityStartDatePage, NormalMode, emptyUserAnswers) mustBe controllers.add.routes.AuthorityEndController.onPageLoad(NormalMode)
      }

      "go from AuthorityEnd to AuthorityEndDate when 'set date' is chosen" in {
        val userAnswers = emptyUserAnswers.set(AuthorityEndPage, AuthorityEnd.Setdate)(AuthorityEnd.writes).success.value
        navigator.nextPage(AuthorityEndPage, NormalMode, userAnswers) mustBe controllers.add.routes.AuthorityEndDateController.onPageLoad(NormalMode)
      }

      "go from AuthorityEnd to ShowBalance when 'further notice' is chosen" in {
        val userAnswers = emptyUserAnswers.set(AuthorityEndPage, AuthorityEnd.Indefinite)(AuthorityEnd.writes).success.value
        navigator.nextPage(AuthorityEndPage, NormalMode, userAnswers) mustBe controllers.add.routes.ShowBalanceController.onPageLoad(NormalMode)
      }

      "go from AuthorityEndDate to ShowBalance" in {
        navigator.nextPage(AuthorityEndDatePage, NormalMode, emptyUserAnswers) mustBe controllers.add.routes.ShowBalanceController.onPageLoad(NormalMode)
      }

      "go from ShowBalance to AuthorisedUser" in {
        navigator.nextPage(ShowBalancePage, NormalMode, emptyUserAnswers) mustBe controllers.add.routes.AuthorisedUserController.onPageLoad()
      }

      "backLink on showbalance should navigate to EndDate when AuthorityEndDate is set Date" in {
        val userAnswers = emptyUserAnswers
          .set(AuthorityEndPage, AuthorityEnd.Setdate)(AuthorityEnd.writes).success.value
        navigator.backLinkRouteForShowBalancePage(NormalMode, userAnswers) mustBe controllers.add.routes.AuthorityEndDateController.onPageLoad(NormalMode)
      }

      "backLink on showbalance should navigate to EndDate when AuthorityEndDate is Indefinite" in {
        val userAnswers = emptyUserAnswers
          .set(AuthorityEndPage, AuthorityEnd.Indefinite)(AuthorityEnd.writes).success.value
        navigator.backLinkRouteForShowBalancePage(NormalMode, userAnswers) mustBe controllers.add.routes.AuthorityEndController.onPageLoad(NormalMode)
      }

      "backLink on EndPage should navigate to AuthorityStartDate when setDate is selected" in {
        val userAnswers = emptyUserAnswers
          .set(AuthorityStartPage, AuthorityStart.Setdate)(AuthorityStart.writes).success.value
        navigator.backLinkRouteForAuthorityEndPage(NormalMode, userAnswers) mustBe controllers.add.routes.AuthorityStartDateController.onPageLoad(NormalMode)
      }

      "backLink on EndPage should navigate to AuthorityStart when AuthorityStart is today" in {
        val userAnswers = emptyUserAnswers
          .set(AuthorityStartPage, AuthorityStart.Today)(AuthorityStart.writes).success.value
        navigator.backLinkRouteForAuthorityEndPage(NormalMode, userAnswers) mustBe controllers.add.routes.AuthorityStartController.onPageLoad(NormalMode)
      }

      "backLink on Accounts should navigate to EoriNumber" in {
        navigator.backLinkRoute(NormalMode, controllers.add.routes.EoriNumberController.onPageLoad(NormalMode)) mustBe controllers.add.routes.EoriNumberController.onPageLoad(NormalMode)
      }

    }

    "in Check mode" must {

      "go to AuthorisedUser from a page that doesn't exist in the edit route map" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, emptyUserAnswers) mustBe controllers.add.routes.AuthorisedUserController.onPageLoad()
      }

      "go from Accounts to AuthorisedUser" in {
        navigator.nextPage(AccountsPage, CheckMode, emptyUserAnswers) mustBe controllers.add.routes.AuthorisedUserController.onPageLoad()
      }

      "go from EoriNumber to AuthorisedUser" in {
        navigator.nextPage(EoriNumberPage, CheckMode, emptyUserAnswers) mustBe controllers.add.routes.AuthorityStartController.onPageLoad(NormalMode)
      }

      "go from AuthorityStart to AuthorityStartDate when 'set date' is chosen" in {
        val userAnswers = emptyUserAnswers.set(AuthorityStartPage, AuthorityStart.Setdate)(AuthorityStart.writes).success.value
        navigator.nextPage(AuthorityStartPage, CheckMode, userAnswers) mustBe controllers.add.routes.AuthorityStartDateController.onPageLoad(CheckMode)
      }

      "go from AuthorityStart to AuthorisedUser when 'today' is chosen" in {
        val userAnswers = emptyUserAnswers.set(AuthorityStartPage, AuthorityStart.Today)(AuthorityStart.writes).success.value
        navigator.nextPage(AuthorityStartPage, CheckMode, userAnswers) mustBe controllers.add.routes.AuthorisedUserController.onPageLoad()
      }

      "go from AuthorityStartDate to AuthorityEndDate when 'set date' is chosen for AuthorityEnd" in {
        val userAnswers = emptyUserAnswers.set(AuthorityEndPage, AuthorityEnd.Setdate)(AuthorityEnd.writes).success.value
        navigator.nextPage(AuthorityStartDatePage, CheckMode, userAnswers) mustBe controllers.add.routes.AuthorityEndDateController.onPageLoad(CheckMode)
      }

      "go from AuthorityStartDate to AuthorisedUser when 'indefinite' is chosen for AuthorityEnd" in {
        val userAnswers = emptyUserAnswers.set(AuthorityEndPage, AuthorityEnd.Indefinite)(AuthorityEnd.writes).success.value
        navigator.nextPage(AuthorityStartDatePage, CheckMode, userAnswers) mustBe controllers.add.routes.AuthorisedUserController.onPageLoad()
      }

      "go from AuthorityEnd to AuthorityEndDate when 'set date' is chosen" in {
        val userAnswers = emptyUserAnswers.set(AuthorityEndPage, AuthorityEnd.Setdate)(AuthorityEnd.writes).success.value
        navigator.nextPage(AuthorityEndPage, CheckMode, userAnswers) mustBe controllers.add.routes.AuthorityEndDateController.onPageLoad(CheckMode)
      }

      "go from AuthorityEnd to AuthorisedUser when 'further notice' is chosen" in {
        val userAnswers = emptyUserAnswers.set(AuthorityEndPage, AuthorityEnd.Indefinite)(AuthorityEnd.writes).success.value
        navigator.nextPage(AuthorityEndPage, CheckMode, userAnswers) mustBe controllers.add.routes.AuthorisedUserController.onPageLoad()
      }

      "go from ShowBalance to AuthorisedUser" in {
        navigator.nextPage(ShowBalancePage, CheckMode, emptyUserAnswers) mustBe controllers.add.routes.AuthorisedUserController.onPageLoad()
      }

      "backLink should navigate to AuthorisedUser when in check mode" in {
        navigator.backLinkRouteForEORINUmberPage(CheckMode) mustBe controllers.add.routes.AuthorisedUserController.onPageLoad
      }

      "backLink should navigate to manage authorities when in normal mode" in {
        navigator.backLinkRouteForEORINUmberPage(NormalMode) mustBe controllers.routes.ManageAuthoritiesController.onPageLoad()
      }

      "backLink on showbalance should navigate to AuthorisedUser" in {
        val userAnswers = emptyUserAnswers
          .set(AuthorityEndPage, AuthorityEnd.Setdate)(AuthorityEnd.writes).success.value
        navigator.backLinkRouteForShowBalancePage(CheckMode, userAnswers) mustBe controllers.add.routes.AuthorisedUserController.onPageLoad()
      }

      "backLink on EndPage should navigate to AuthorisedUser" in {
        val userAnswers = emptyUserAnswers
          .set(AuthorityStartPage, AuthorityStart.Setdate)(AuthorityStart.writes).success.value
        navigator.backLinkRouteForShowBalancePage(CheckMode, userAnswers) mustBe controllers.add.routes.AuthorisedUserController.onPageLoad()
      }

      "backLink on Accounts should navigate to AuthorisedUser" in {
        navigator.backLinkRoute(CheckMode, controllers.add.routes.AuthorisedUserController.onPageLoad()) mustBe controllers.add.routes.AuthorisedUserController.onPageLoad()
      }

    }
  }
}
