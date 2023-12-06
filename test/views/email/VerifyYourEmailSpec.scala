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

package views.email

import base.SpecBase
import config.FrontendAppConfig
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeRequest, Helpers}
import views.html.email.verify_your_email

class VerifyYourEmailSpec extends SpecBase {

  "verify your email view" should {
    "have page title" in new Setup {
      view().title() mustBe "cf.verify.your.email.title - service.name - site.govuk"
    }

    "have email frontend url" in new Setup {
      view().getElementsByClass("govuk-button").attr("href") mustBe "/next/page"
    }

    "have heading" in new Setup {
      view().getElementsByClass("govuk-heading-xl").html() mustBe "cf.verify.your.email.heading"
    }

    "display static text and email address" in new Setup {
      view().getElementsByClass("govuk-body").first().html() mustBe "cf.verify.your.email.p1"
      view().getElementsByClass("govuk-body").eq(1).html() mustBe "cf.verify.your.email.p2"
      view().getElementsByClass("govuk-body").eq(2).html() mustBe "cf.verify.your.email.p3"
      view().body().text().contains("some@email.com")
    }

    "have button" in new Setup {
      view().getElementsByClass("govuk-button").text().contains("cf.verify.your.email.change.button")
    }
  }

  trait Setup  {
    implicit val csrfRequest: FakeRequest[AnyContentAsEmpty.type] = fakeRequest("GET", "/some/resource/path")
    val app = applicationBuilder().build()

    implicit val appConfig = app.injector.instanceOf[FrontendAppConfig]
    implicit val messages: Messages = Helpers.stubMessages()

    def view() = Jsoup.parse(app.injector.instanceOf[verify_your_email].apply("/next/page", Some("some@email.com")).body)
  }

}
