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
import views.html.ManageAuthoritiesGBNAuthorityView

class ManageAuthoritiesGBNAuthorityViewSpec extends SpecBase {

  "ManageAuthoritiesGBNAuthorityView" should {

    "get heading title" in new Setup {
      view().getElementById("manageAuthorities.heading").text mustBe s"manageAuthorities.title"
    }
  }

  trait Setup {
    implicit val csrfRequest: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest("GET", "/some/resource/path")

    implicit val messages: Messages = Helpers.stubMessages()

    def view(): Document = Jsoup.parse(
      application(Some(emptyUserAnswers)).injector
        .instanceOf[ManageAuthoritiesGBNAuthorityView]
        .apply()(request, messages, appConfig)
        .body
    )
  }
}
