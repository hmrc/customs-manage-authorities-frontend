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

package views

import base.SpecBase
import config.FrontendAppConfig
import forms.AccountsFormProvider
import models.domain.{AccountStatusClosed, AccountStatusOpen, CDSCashBalance, CashAccount}
import models.{AuthorisedAccounts, CheckMode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.{FakeRequest, Helpers}
import views.html.AccountsView
import utils.StringUtils.emptyString

class AccountsViewSpec extends SpecBase {

  "AccountsView" should {
    "when back-link is clicked returns to previous page on Normal Mode" in new Setup {
      normalModeView().getElementsByClass("govuk-back-link")
        .attr("href") mustBe s"/customs/manage-authorities/add-authority/eori-number"
    }

    "when back-link is clicked returns to previous page on Check Mode" in new Setup {
      checkModeView().getElementsByClass("govuk-back-link")
        .attr("href") mustBe s"/customs/manage-authorities/add-authority/check-answers"
    }

    "display error if form has any error" in new Setup {
      invalidModeView().getElementById("value-error").childNodes().size() must be > 0
    }
  }

  trait Setup {
    implicit val csrfRequest: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest("GET", "/some/resource/path")

    val app: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

    implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
    implicit val messages: Messages = Helpers.stubMessages()

    private lazy val normalModeBackLinkRoute: Call = controllers.add.routes.EoriNumberController.onPageLoad(NormalMode)
    private lazy val checkModeBackLinkRoute: Call = controllers.add.routes.AuthorisedUserController.onPageLoad()

    private val formProvider = new AccountsFormProvider()
    private val form = formProvider()
    private val invalidForm = formProvider().bind(Map("value" -> emptyString))

    private val ownerEori = "GB123456789012"
    private val enteredEori = "GB9876543210000"

    private val bigDecimalAmount = 100.00

    val answerAccounts: List[CashAccount] =
      List(CashAccount("12345", ownerEori, AccountStatusOpen, CDSCashBalance(Some(bigDecimalAmount))))

    def normalModeView(): Document =
      Jsoup.parse(app.injector.instanceOf[AccountsView].apply(
        form,
        AuthorisedAccounts(
          Seq.empty,
          answerAccounts,
          Seq(CashAccount("23456", ownerEori, AccountStatusClosed, CDSCashBalance(Some(bigDecimalAmount)))),
          Seq.empty,
          enteredEori),
        NormalMode,
        normalModeBackLinkRoute).body
      )

    def checkModeView(): Document =
      Jsoup.parse(app.injector.instanceOf[AccountsView].apply(
        form,
        AuthorisedAccounts(
          Seq.empty,
          answerAccounts,
          Seq(
            CashAccount("23456", ownerEori, AccountStatusClosed, CDSCashBalance(Some(bigDecimalAmount)))),
          Seq.empty,
          enteredEori),
        CheckMode,
        checkModeBackLinkRoute).body
      )

    def invalidModeView(): Document =
      Jsoup.parse(app.injector.instanceOf[AccountsView].apply(
        invalidForm,
        AuthorisedAccounts(
          Seq.empty,
          answerAccounts,
          Seq(CashAccount("23456", ownerEori, AccountStatusClosed, CDSCashBalance(Some(bigDecimalAmount)))),
          Seq.empty,
          enteredEori),
        NormalMode,
        normalModeBackLinkRoute).body
      )
  }
}
