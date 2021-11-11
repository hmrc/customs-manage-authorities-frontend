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

package views

import base.SpecBase
import config.FrontendAppConfig
import forms.AccountsFormProvider
import models.domain.{AccountStatusClosed, AccountStatusOpen, CDSCashBalance, CashAccount}
import models.{AuthorisedAccounts, CheckMode, NormalMode}
import org.jsoup.Jsoup
import org.scalatest.Matchers._
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.{FakeRequest, Helpers}
import views.html.AccountsView

class AccountsViewSpec extends SpecBase with MockitoSugar {


  "AccountsView" should {
     "when back-link is clicked returns to previous page on Normal Mode" in new Setup {
       normalModeView().getElementById("back-link").attr("href") mustBe s"/customs/manage-authorities/add-authority/eori-number"
     }

    "when back-link is clicked returns to previous page on Check Mode" in new Setup {
      checkModeView().getElementById("back-link").attr("href") mustBe s"/customs/manage-authorities/add-authority/check-answers"
    }
    }


  trait Setup  {
    implicit val csrfRequest: FakeRequest[AnyContentAsEmpty.type] = fakeRequest("GET", "/some/resource/path")
    val app = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
    implicit val appConfig = app.injector.instanceOf[FrontendAppConfig]
    implicit val messages: Messages = Helpers.stubMessages()
    private lazy val normalModeBackLinkRoute: Call = controllers.add.routes.EoriNumberController.onPageLoad(NormalMode)
    private lazy val checkModeBackLinkRoute: Call = controllers.add.routes.AuthorisedUserController.onPageLoad()

    private val formProvider = new AccountsFormProvider()
    private val form = formProvider()
    val answerAccounts = List(CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00))))
    def normalModeView() = Jsoup.parse(app.injector.instanceOf[AccountsView].apply(form, AuthorisedAccounts(Seq.empty, answerAccounts, Seq(
      CashAccount("23456", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(100.00)))
    ), Seq.empty, "GB9876543210000"),NormalMode,normalModeBackLinkRoute).body)

    def checkModeView() = Jsoup.parse(app.injector.instanceOf[AccountsView].apply(form, AuthorisedAccounts(Seq.empty, answerAccounts, Seq(
      CashAccount("23456", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(100.00)))
    ), Seq.empty, "GB9876543210000"),CheckMode,checkModeBackLinkRoute).body)
  }
}
