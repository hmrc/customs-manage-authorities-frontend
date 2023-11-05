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

package views.remove

import base.SpecBase
import config.FrontendAppConfig
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeRequest, Helpers}
import views.html.remove.RemoveConfirmationView

class RemoveConfirmationViewSpec extends SpecBase {

  "RemoveConfirmationView" should {
    "render the view with correct content" in new Setup {
      val view = createView("testEori", Some("Test Company Name"))

      view.getElementsByClass("remove-confirm-panel").text() must include("")
      view.getElementsByClass("remove-p1").text() must include("You may want to tell the company that their authority has been removed.")
      view.getElementsByClass("return-link").text() must include("Back to manage your account authorities")
    }

  }

  trait Setup {
    val app = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

    implicit val appConfig = app.injector.instanceOf[FrontendAppConfig]
    implicit val messages: Messages = Helpers.stubMessages()

    implicit val csrfRequest: FakeRequest[AnyContentAsEmpty.type] = fakeRequest(
      "GET", "/some/resource/path")

    def createView(eori: String, companyName: Option[String]) = Jsoup.parse(
      app.injector.instanceOf[RemoveConfirmationView].apply(eori, companyName).body)
  }
}
