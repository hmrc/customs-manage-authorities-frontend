/*
 * Copyright 2023 HM Revenue & Customs
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

package views

import base.SpecBase
import config.FrontendAppConfig
import forms.AuthorisedUserFormProviderWithConsent
import models.domain.{AccountStatusOpen, AuthorisedUser, CDSAccount, CDSCashBalance, CashAccount, DutyDefermentAccount, DutyDefermentBalance, GeneralGuaranteeAccount, GeneralGuaranteeBalance}
import models.{AuthorityStart, CompanyDetails, EoriDetailsCorrect, ShowBalance, UserAnswers}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add._
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeRequest, Helpers}
import services.DateTimeService
import viewmodels.CheckYourAnswersHelper
import views.html.add.AuthorisedUserView

import java.time.LocalDateTime

class AuthorisedUserViewSpec extends SpecBase with MockitoSugar {

  "AuthorisedUserView" should {
    "have back link" in new Setup {
      view()
        .getElementsByClass("govuk-back-link")
        .attr("href") mustBe s"/customs/manage-authorities/add-authority/your-details"
    }

    "Display Company Name" in new Setup {

      override val cashAccount: CashAccount = CashAccount(
        "12345",
        "XI123456789012",
        AccountStatusOpen,
        CDSCashBalance(Some(100.00)))
      override val dutyDeferment: DutyDefermentAccount = DutyDefermentAccount(
        "67890",
        "XI210987654321",
        AccountStatusOpen,
        DutyDefermentBalance(None, None, None, None),
        isNiAccount = true)

      override val selectedAccounts: List[CDSAccount] = List(cashAccount, dutyDeferment)

      override val userAnswersTodayToIndefinite: UserAnswers = UserAnswers("id")
        .set(AccountsPage, selectedAccounts).success.value
        .set(EoriNumberPage, CompanyDetails("GB123456789012", Some("companyName"))).success.value
        .set(AuthorityStartPage, AuthorityStart.Today)(AuthorityStart.writes).success.value
        .set(EoriDetailsCorrectPage, EoriDetailsCorrect.Yes)(EoriDetailsCorrect.writes).success.value
        .set(ShowBalancePage, ShowBalance.Yes)(ShowBalance.writes).success.value
        .set(AuthorityDetailsPage, AuthorisedUser("", "")).success.value


      override val userAnswers = userAnswersTodayToIndefinite.set(AccountsPage, List(dutyDeferment)).success.value
      override val helper = CheckYourAnswersHelper(userAnswers, mockDateTimeService)

      val pageView: Document = Jsoup.parse(app.injector.instanceOf[AuthorisedUserView].apply(
        new AuthorisedUserFormProviderWithConsent().apply(), helper).body)

      pageView.getElementsByClass(
        "govuk-summary-list__row").get(1).text() mustBe "checkYourAnswers.companyName.label companyName"
    }

    "display (Northern Ireland) text next to Duty deferment:<Acc Number> if " +
      "EORI is of Northern Ireland" in new Setup {

      override val cashAccount: CashAccount = CashAccount(
        "12345",
        "XI123456789012",
        AccountStatusOpen,
        CDSCashBalance(Some(100.00)))
      override val dutyDeferment: DutyDefermentAccount = DutyDefermentAccount(
        "67890",
        "XI210987654321",
        AccountStatusOpen,
        DutyDefermentBalance(None, None, None, None),
        isNiAccount = true)

      override val selectedAccounts: List[CDSAccount] = List(cashAccount, dutyDeferment)

      override val userAnswersTodayToIndefinite: UserAnswers = UserAnswers("id")
        .set(AccountsPage, selectedAccounts).success.value
        .set(EoriNumberPage, CompanyDetails("GB123456789012", Some("companyName"))).success.value
        .set(AuthorityStartPage, AuthorityStart.Today)(AuthorityStart.writes).success.value
        .set(EoriDetailsCorrectPage, EoriDetailsCorrect.Yes)(EoriDetailsCorrect.writes).success.value
        .set(ShowBalancePage, ShowBalance.Yes)(ShowBalance.writes).success.value
        .set(AuthorityDetailsPage, AuthorisedUser("", "")).success.value


      override val userAnswers = userAnswersTodayToIndefinite.set(AccountsPage, List(dutyDeferment)).success.value
      override val helper = CheckYourAnswersHelper(userAnswers, mockDateTimeService)

      val pageView: Document = Jsoup.parse(app.injector.instanceOf[AuthorisedUserView].apply(
        new AuthorisedUserFormProviderWithConsent().apply(), helper).body)

      pageView.getElementsByClass("govuk-summary-list__row")
        .get(2).text() mustBe
        "accounts.checkYourAnswersLabel.singular accounts.type.dutyDeferment accounts.ni: 67890" +
          " site.change checkYourAnswers.accounts.hidden"
    }

    "do not display (Northern Ireland) text next to Account Type:<Acc Number> if " +
      "Account type is not Duty deferment" in new Setup {

      override val cashAccount: CashAccount = CashAccount(
        "12345",
        "GB123456789012",
        AccountStatusOpen,
        CDSCashBalance(Some(100.00)))

      override val selectedAccounts: List[CDSAccount] = List(cashAccount)

      override val userAnswersTodayToIndefinite: UserAnswers = UserAnswers("id")
        .set(AccountsPage, selectedAccounts).success.value
        .set(EoriNumberPage, CompanyDetails("GB123456789012", Some("companyName"))).success.value
        .set(AuthorityStartPage, AuthorityStart.Today)(AuthorityStart.writes).success.value
        .set(EoriDetailsCorrectPage, EoriDetailsCorrect.Yes)(EoriDetailsCorrect.writes).success.value
        .set(ShowBalancePage, ShowBalance.Yes)(ShowBalance.writes).success.value
        .set(AuthorityDetailsPage, AuthorisedUser("", "")).success.value


      override val userAnswers = userAnswersTodayToIndefinite.set(AccountsPage, List(cashAccount)).success.value
      override val helper = CheckYourAnswersHelper(userAnswers, mockDateTimeService)

      val pageView: Document = Jsoup.parse(app.injector.instanceOf[AuthorisedUserView].apply(
        new AuthorisedUserFormProviderWithConsent().apply(), helper).body)

      pageView.getElementsByClass("govuk-summary-list__row")
        .get(2).text() mustBe
        "accounts.checkYourAnswersLabel.singular accounts.type.cash:" +
          " 12345 site.change checkYourAnswers.accounts.hidden"
    }

    "do not display (Northern Ireland) text next to Account type:<Acc Number> if " +
      "EORI is of GB and account type is Duty deferment" in new Setup {

      override val cashAccount: CashAccount = CashAccount(
        "12345",
        "GB123456789012",
        AccountStatusOpen,
        CDSCashBalance(Some(100.00)))

      override val dutyDeferment: DutyDefermentAccount = DutyDefermentAccount(
        "67890",
        "GB210987654321",
        AccountStatusOpen,
        DutyDefermentBalance(None, None, None, None))

      override val selectedAccounts: List[CDSAccount] = List(cashAccount, dutyDeferment)

      override val userAnswersTodayToIndefinite: UserAnswers = UserAnswers("id")
        .set(AccountsPage, selectedAccounts).success.value
        .set(EoriNumberPage, CompanyDetails("GB123456789012", Some("companyName"))).success.value
        .set(AuthorityStartPage, AuthorityStart.Today)(AuthorityStart.writes).success.value
        .set(EoriDetailsCorrectPage, EoriDetailsCorrect.Yes)(EoriDetailsCorrect.writes).success.value
        .set(ShowBalancePage, ShowBalance.Yes)(ShowBalance.writes).success.value
        .set(AuthorityDetailsPage, AuthorisedUser("", "")).success.value


      override val userAnswers = userAnswersTodayToIndefinite.set(AccountsPage, List(dutyDeferment)).success.value
      override val helper = CheckYourAnswersHelper(userAnswers, mockDateTimeService)

      val pageView: Document = Jsoup.parse(app.injector.instanceOf[AuthorisedUserView].apply(
        new AuthorisedUserFormProviderWithConsent().apply(), helper).body)

      pageView.getElementsByClass("govuk-summary-list__row")
        .get(2).text() mustBe
        "accounts.checkYourAnswersLabel.singular accounts.type.dutyDeferment:" +
          " 67890 site.change checkYourAnswers.accounts.hidden"
    }
  }

  trait Setup {
    def cashAccount: CashAccount = CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))

    def dutyDeferment: DutyDefermentAccount = DutyDefermentAccount(
      "67890", "GB210987654321", AccountStatusOpen, DutyDefermentBalance(None, None, None, None))

    def generalGuarantee: GeneralGuaranteeAccount = GeneralGuaranteeAccount(
      "54321", "GB000000000000", AccountStatusOpen, Some(GeneralGuaranteeBalance(50.00, 50.00)))

    def selectedAccounts: List[CDSAccount] = List(cashAccount, dutyDeferment, generalGuarantee)

    def userAnswersTodayToIndefinite: UserAnswers = UserAnswers("id")
      .set(AccountsPage, selectedAccounts).success.value
      .set(EoriNumberPage, CompanyDetails("GB123456789012", Some("companyName"))).success.value
      .set(AuthorityStartPage, AuthorityStart.Today)(AuthorityStart.writes).success.value
      .set(EoriDetailsCorrectPage, EoriDetailsCorrect.Yes)(EoriDetailsCorrect.writes).success.value
      .set(ShowBalancePage, ShowBalance.Yes)(ShowBalance.writes).success.value
      .set(AuthorityDetailsPage, AuthorisedUser("", "")).success.value

    val mockDateTimeService: DateTimeService = mock[DateTimeService]
    when(mockDateTimeService.localTime()).thenReturn(LocalDateTime.now())

    implicit val csrfRequest: FakeRequest[AnyContentAsEmpty.type] = fakeRequest("GET", "/some/resource/path")
    val app: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
    implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
    implicit val messages: Messages = Helpers.stubMessages()

    def userAnswers: UserAnswers = userAnswersTodayToIndefinite.set(AccountsPage, List(cashAccount)).success.value

    def helper: CheckYourAnswersHelper = CheckYourAnswersHelper(userAnswers, mockDateTimeService)

    private val formProvider = new AuthorisedUserFormProviderWithConsent()
    private val form = formProvider()

    def view(): Document = Jsoup.parse(app.injector.instanceOf[AuthorisedUserView].apply(form, helper).body)
  }

}
