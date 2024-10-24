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

package views.components

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import views.html.components.link_twoLinks

class LinkTwoLinksSpec extends SpecBase {

  "link_twoLinks" should {

    "render correctly two links with correct IDs and attributes" in new Setup {
      val html: HtmlFormat.Appendable = view(
        firstLinkMessage = "First Link",
        firstLinkHref = "gov.uk/first",
        firstLinkId = Some("first-link"),
        secondLinkMessage = "Second Link",
        secondLinkHref = "gov.uk/second",
        secondLinkId = Some("second-link"))

      val document: Document = Jsoup.parse(html.toString())

      document.select("a#first-link").attr("href") mustBe "gov.uk/first"
      document.select("a#first-link").text() mustBe "First Link"
      document.select("a#first-link").hasClass("govuk-link govuk-!-margin-right-4") mustBe true

      document.select("a#second-link").attr("href") mustBe "gov.uk/second"
      document.select("a#second-link").text() mustBe "Second Link"
      document.select("a#second-link").hasClass("govuk-link") mustBe true
    }
  }

  trait Setup {
    val app: Application = applicationBuilder().build()
    implicit val msg: Messages = messages(app)

    val view: link_twoLinks = app.injector.instanceOf[link_twoLinks]
  }
}
