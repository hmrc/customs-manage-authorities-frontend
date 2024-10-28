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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeRequest, Helpers}
import views.html.NoAccountsView

class NoAccountsViewSpec extends SpecBase {

  "NoAccountsView" should {
    "return to home page when back link is clicked" in new Setup {
      view().getElementsByClass("govuk-back-link").attr("href") mustBe s"$homeUrl/"
    }

    "display header" in new Setup {
      view().getElementById("manageAuthorities.heading").text mustBe  messages("manageAuthorities.heading")
    }

    "display link to find accounts" in new Setup {
      view().getElementById("manageAuthorities-noAccounts-link").text mustBe
        messages("cf.account.authorized-to-view.title")
    }

    "goto find accounts you have auth to use when the link is clicked" in new Setup {
      view().getElementById("manageAuthorities-noAccounts-link").attr("href") mustBe s"$homeUrl/authorized-to-view"
    }

  }

  trait Setup {
    implicit val csrfRequest: FakeRequest[AnyContentAsEmpty.type] = fakeRequest("GET", "/some/resource/path")

    val app: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
    implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
    implicit val messages: Messages = Helpers.stubMessages()

    val homeUrl: String = "http://localhost:9876/customs/payment-records"

    def view(): Document = Jsoup.parse(app.injector.instanceOf[NoAccountsView].apply().body)
  }
}
