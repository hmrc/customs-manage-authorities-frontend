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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.Application
import play.api.i18n.Messages
import play.api.test.Helpers.running
import base.SpecBase
import views.html.components.addConfirmationPanel

class AddConfirmationPanelSpec extends SpecBase {

  "AddConfirmationPanel view" should {
    "display the correct title, header, and company name when all fields are provided" in new Setup {

      override val view: Document = Jsoup.parse(
        app.injector
          .instanceOf[addConfirmationPanel]
          .apply(
            message = Some("Success"),
            companyName = Some("TestCompany"),
            eori = Some("GB123456789000"),
            startDate = Some("01-01-2021"),
            divClass = Some("custom-div-class"),
            h1Class = Some("custom-h1-class")
          )
          .body
      )

      running(app) {
        view.getElementsByTag("h1").html() mustBe "Success"

        firstElement.getElementsByTag("p").get(0).html() must include(
          messages(app)("addConfirmation.body.eori.number")
        )
        firstElement
          .getElementsByTag("strong")
          .get(0)
          .html() mustBe "GB123456789000"

        firstElement.getElementsByTag("p").get(1).html() must include(
          messages(app)("addConfirmation.body.company.name")
        )
        firstElement
          .getElementsByTag("strong")
          .get(1)
          .html() mustBe "TestCompany"

        firstElement.getElementsByTag("p").get(2).html() must include(
          messages(app)("addConfirmation.body.setDate", "01-01-2021")
        )
      }
    }

    "display the correct title, header without company name and start date when they are not provided" in new Setup {

      override val view: Document = Jsoup.parse(
        app.injector
          .instanceOf[addConfirmationPanel]
          .apply(
            message = Some("Success"),
            companyName = None,
            eori = Some("GB123456789000"),
            startDate = None,
            divClass = Some("custom-div-class"),
            h1Class = Some("custom-h1-class")
          )
          .body
      )

      running(app) {
        view.getElementsByTag("h1").html() mustBe "Success"

        firstElement.getElementsByTag("p").get(0).html() must include(
          messages(app)("addConfirmation.body.eori.number")
        )
        firstElement
          .getElementsByTag("strong")
          .get(0)
          .html() mustBe "GB123456789000"
      }
    }
  }

  trait Setup {
    val app: Application = applicationBuilder().build()
    implicit val msg: Messages = messages(app)

    val view: Document

    lazy val bodyElements: Elements = view.getElementsByClass("govuk-panel__body-s")
    lazy val firstElement = bodyElements.get(0)
  }
}
