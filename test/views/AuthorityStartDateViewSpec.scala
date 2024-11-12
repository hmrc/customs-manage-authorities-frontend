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
import forms.AuthorityStartFormProvider
import models.{CheckMode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.{FakeRequest, Helpers}
import views.html.add.AuthorityStartView

class AuthorityStartDateViewSpec extends SpecBase {

  "AuthorityStartDaterView" should {
    "when back-link is clicked returns to previous page on Normal Mode" in new Setup {
      normalModeView().getElementsByClass("govuk-back-link")
        .attr("href") mustBe s"/customs/manage-authorities/add-authority/start"
    }
    "when back-link is clicked returns to previous page on Check Mode" in new Setup {
      checkModeView().getElementsByClass("govuk-back-link")
        .attr("href") mustBe s"/customs/manage-authorities/add-authority/check-answers"
    }
  }

  trait Setup {
    implicit val csrfRequest: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest("GET", "/some/resource/path")

    val app: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

    implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
    implicit val messages: Messages = Helpers.stubMessages()

    private val formProvider = new AuthorityStartFormProvider()
    private val form = formProvider()

    private lazy val normalModeBackLinkRoute: Call =
      controllers.add.routes.AuthorityStartController.onPageLoad(NormalMode)

    private lazy val checkModeBackLinkRoute: Call = controllers.add.routes.AuthorisedUserController.onPageLoad()

    def normalModeView(): Document =
      Jsoup.parse(app.injector.instanceOf[AuthorityStartView].apply(form, NormalMode, normalModeBackLinkRoute).body)

    def checkModeView(): Document =
      Jsoup.parse(app.injector.instanceOf[AuthorityStartView].apply(form, CheckMode, checkModeBackLinkRoute).body)
  }
}
