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

package views.components

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.Application
import play.api.i18n.Messages
import views.html.components.input_text_hint
import views.ViewUtils.{DetailsHint, LabelHint}

class InputTextHintSpec extends SpecBase {

  "InputTextHint" should {
    "display correct view when details and label hint are present" in new Setup {
      val view: Document = Jsoup.parse(app.injector.instanceOf[input_text_hint].apply(
        Option(DetailsHint(detailsSummaryText, detailsText)), Option(LabelHint(labelText)), id
      ).body)

      val detailsHintElement: Element = view.getElementById(s"$id-hint-details")
      val hintLabelElement: Element = view.getElementById(s"$id-hint-text")

      detailsHintElement.getElementById("value-hint-title").html() mustBe
        detailsSummaryText
      detailsHintElement.getElementsByClass("govuk-body").html() mustBe
        detailsText

      hintLabelElement.html() mustBe labelText
    }

    "display correct view when only details hint is present" in new Setup {
      val view: Document = Jsoup.parse(app.injector.instanceOf[input_text_hint].apply(
        detailsHint = Option(DetailsHint(detailsSummaryText, detailsText)), id = id
      ).body)

      val detailsHintElement: Element = view.getElementById(s"$id-hint-details")

      detailsHintElement.getElementById("value-hint-title").html() mustBe
        detailsSummaryText
      detailsHintElement.getElementsByClass("govuk-body").html() mustBe
        detailsText

      intercept[RuntimeException] {
        view.getElementById(s"$id-hint").html()
      }
    }

    "display correct view when only label hint is present" in new Setup {
      val view: Document = Jsoup.parse(app.injector.instanceOf[input_text_hint].apply(
        labelHint = Option(LabelHint(labelText)), id = id
      ).body)

      val labelHintElement: Element = view.getElementById(s"$id-hint-text")

      labelHintElement.html() mustBe labelText

      intercept[RuntimeException] {
        view.getElementById(s"$id-hint-details").html()
      }
    }

    "display correct view when no hint is present" in new Setup {
      val view: Document = Jsoup.parse(
        app.injector.instanceOf[input_text_hint].apply(id = id).body)

      intercept[RuntimeException] {
        view.getElementById(s"$id-hint-details").html()
      }

      intercept[RuntimeException] {
        view.getElementById(s"$id-hint").html()
      }
    }
  }

  trait Setup {
    val app: Application = applicationBuilder().build()
    implicit val msg: Messages = messages(app)

    val id = "value"
    val detailsSummaryText = "summaryText"
    val detailsText = "text"
    val labelText = "labelText"
  }
}
