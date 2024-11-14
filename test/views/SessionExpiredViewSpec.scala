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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeRequest, Helpers}
import views.html.SessionExpiredView

class SessionExpiredViewSpec extends SpecBase {

  "SessionExpired view" should {
    "when back link is clicked returns to start of the journey" in new Setup {
      view().getElementsByClass("govuk-back-link")
        .attr("href") mustBe s"/customs/manage-authorities/manage-account-authorities"
      }
    }


  trait Setup  {
    implicit val csrfRequest: FakeRequest[AnyContentAsEmpty.type] = fakeRequest("GET", "/some/resource/path")
    val app: Application = applicationBuilder().build()

    implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
    implicit val messages: Messages = Helpers.stubMessages()

    def view(): Document = Jsoup.parse(app.injector.instanceOf[SessionExpiredView].apply().body)
  }
}
