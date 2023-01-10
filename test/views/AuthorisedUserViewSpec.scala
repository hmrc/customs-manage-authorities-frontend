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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Mockito.when
import org.scalatest.Matchers._
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeRequest, Helpers}
import services.DateTimeService
import viewmodels.CheckYourAnswersHelper
import views.html.add.AuthorisedUserView
import java.time.LocalDateTime
import models.domain.{AccountStatusOpen, AuthorisedUser, CDSAccount, CDSCashBalance, CashAccount, DutyDefermentAccount, DutyDefermentBalance, GeneralGuaranteeAccount, GeneralGuaranteeBalance}
import models.{AuthorityStart, CompanyDetails, EoriDetailsCorrect, ShowBalance, UserAnswers}
import pages.add.{AccountsPage, AuthorityDetailsPage, AuthorityStartPage, EoriDetailsCorrectPage, EoriNumberPage, ShowBalancePage}

class AuthorisedUserViewSpec extends SpecBase with MockitoSugar {

  "AuthorisedUserView" should {
      "have back link" in new Setup {
        view()
          .getElementsByClass("govuk-back-link")
          .attr("href") mustBe s"/customs/manage-authorities/add-authority/your-details"
      }
    }

  trait Setup {

    val cashAccount: CashAccount = CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))
    val dutyDeferment: DutyDefermentAccount = DutyDefermentAccount("67890", "GB210987654321", AccountStatusOpen, DutyDefermentBalance(None, None, None, None))
    val generalGuarantee: GeneralGuaranteeAccount = GeneralGuaranteeAccount("54321", "GB000000000000", AccountStatusOpen, Some(GeneralGuaranteeBalance(50.00, 50.00)))

    val selectedAccounts: List[CDSAccount] = List(cashAccount, dutyDeferment, generalGuarantee)

    val userAnswersTodayToIndefinite: UserAnswers = UserAnswers("id")
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


    val userAnswers = userAnswersTodayToIndefinite.set(AccountsPage, List(cashAccount)).success.value
    val helper = CheckYourAnswersHelper(userAnswers, mockDateTimeService)

    private val formProvider = new AuthorisedUserFormProviderWithConsent()
    private val form = formProvider()
    def view(): Document = Jsoup.parse(app.injector.instanceOf[AuthorisedUserView].apply(form,helper).body)
  }
}
