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
import play.twirl.api.{Html, HtmlFormat}
import views.html.components.eori_text


class EoriTextSpec extends SpecBase {

	"EoriText" should {

		"render correctly with a provided ID" in new Setup {
			val html: HtmlFormat.Appendable = view(
				"test.title",
				Html("Test content"),
				Some("test-id")
			)

			val document: Document = Jsoup.parse(html.toString())

			document.select("div").attr("id") mustBe "test-id"

			document.select("p.govuk-heading-s").text() mustBe msg("test.title")

			document.select("p.govuk-body").text() mustBe "Test content"
		}

		"render correctly without a provided ID" in new Setup {
			val html: HtmlFormat.Appendable = view("test.title", Html("Test content"), None)
			val document: Document = Jsoup.parse(html.toString())

			document.select("div").hasAttr("id") mustBe false

			document.select("p.govuk-heading-s").text() mustBe msg("test.title")

			document.select("p.govuk-body").text() mustBe "Test content"
		}
	}

	trait Setup {
		val app: Application = applicationBuilder().build()
		implicit val msg: Messages = messages(app)

		val view = app.injector.instanceOf[eori_text]
	}
}
