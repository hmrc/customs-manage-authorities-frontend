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

package views.email

import base.SpecBase
import config.FrontendAppConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.email.undeliverable_email

class UndeliverableEmailSpec extends SpecBase {

  "view" should {
    "display correct guidance and text" in new Setup {
      view.title() mustBe
        s"${messages("cf.undeliverable.email.title")} - ${messages("service.name")} - GOV.UK"

      view.getElementsByTag("h1").text() mustBe messages("cf.undeliverable.email.heading")

      view.text().contains(messages("cf.undeliverable.email.p1")) mustBe true
      view.html.contains(messages("cf.undeliverable.email.p2", email))

      view.text().contains(messages("cf.undeliverable.email.verify.heading")) mustBe true
      view.text().contains(messages("cf.undeliverable.email.verify.text.p1")) mustBe true
      view.text().contains(messages("cf.undeliverable.email.change.heading")) mustBe true

      view.text().contains(messages("cf.undeliverable.email.change.text.p1")) mustBe true
      view.text().contains(messages("cf.undeliverable.email.change.text.p2")) mustBe true

      view.text().contains(messages("cf.undeliverable.email.link-text")) mustBe true

      view.toString must include(nextPageUrl)
      view.text().contains(email.get) mustBe true
    }

    "not display the email paragraph if there is no email" in new Setup {
      viewWithNoEmail.text().contains(email.get) mustBe false
    }
  }

  trait Setup {
    val app: Application      = applicationBuilder().build()
    val nextPageUrl           = "test_url"
    val email: Option[String] = Some("test@test.com")

    implicit val appConfig: FrontendAppConfig                 = app.injector.instanceOf[FrontendAppConfig]
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")
    implicit val msg: Messages                                = messages

    // Pass the email parameter when creating the view instance
    val view: Document = Jsoup.parse(app.injector.instanceOf[undeliverable_email].apply(nextPageUrl, email).body)

    // Create the view instance without providing an email
    val viewWithNoEmail: Document =
      Jsoup.parse(app.injector.instanceOf[undeliverable_email].apply(nextPageUrl, None).body)
  }
}
