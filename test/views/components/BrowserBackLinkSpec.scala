/*
 * Copyright 2025 HM Revenue & Customs
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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.test.Helpers.{contentAsString, running}
import play.twirl.api.HtmlFormat
import base.SpecBase
import play.api.test.Helpers.defaultAwaitTimeout
import views.html.components.browserBackLink

class BrowserBackLinkSpec extends SpecBase {

  "BrowserBackLink component" should {

    "render the back link with correct attributes and text" in new Setup {
      running(app) {
        val output: HtmlFormat.Appendable = browserBackLinkView(
          href = "/url",
        )(messages(app))

        val html: Document = Jsoup.parse(contentAsString(output))

        val linkElement = html.getElementById("browser-back-link")
        linkElement.attr("href") mustBe "/url"
      }
    }
  }

  trait Setup {
    val app: Application                       = applicationBuilder().build()
    val browserBackLinkView: browserBackLink   = app.injector.instanceOf[browserBackLink]
  }
}
