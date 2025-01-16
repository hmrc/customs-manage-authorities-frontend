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
import views.html.components.editConfirmPanel

class EditConfirmPanelSpec extends SpecBase {

  "EditConfirmPanel view" should {
    "display the correct title, header and company name when company name is provided" in new SetUp {

      override val view: Document = Jsoup.parse(
        app.injector
          .instanceOf[editConfirmPanel]
          .apply(Option("TestCompany"), Option("GB123456789000"))
          .body
      )

      running(app) {
        view.getElementsByTag("h1").html() mustBe
          messages("editConfirmation.heading")

        firstElement.getElementsByTag("p").get(0).html() must include(
          messages("editConfirmation.body.eori.number")
        )
        firstElement
          .getElementsByTag("strong")
          .get(0)
          .html() mustBe "GB123456789000"

        firstElement.getElementsByTag("p").get(1).html() must include(
          messages("editConfirmation.body.company.name")
        )
        firstElement
          .getElementsByTag("strong")
          .get(1)
          .html() mustBe "TestCompany"

        firstElement.getElementsByTag("p").last().html() must include(
          messages("editConfirmation.body.changes")
        )
      }
    }

    "display the correct title, header without company name when company name is not provided" in new SetUp {

      override val view: Document = Jsoup.parse(
        app.injector
          .instanceOf[editConfirmPanel]
          .apply(None, Option("GB123456789000"))
          .body
      )

      running(app) {
        view.getElementsByTag("h1").html() mustBe
          messages("editConfirmation.heading")

        firstElement.getElementsByTag("p").get(0).html() must include(
          messages("editConfirmation.body.eori.number")
        )
        firstElement
          .getElementsByTag("strong")
          .get(0)
          .html() mustBe "GB123456789000"

        firstElement.getElementsByTag("p").last().html() must include(
          messages("editConfirmation.body.changes")
        )
      }
    }
  }

  trait SetUp {
    val app: Application       = applicationBuilder().build()
    implicit val msg: Messages = messages

    val view: Document

    lazy val bodyElements: Elements = view.getElementsByClass("govuk-panel__body-s")
    lazy val firstElement           = bodyElements.get(0)
  }
}
