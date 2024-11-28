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

package views.remove

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalatest.Assertion
import utils.ViewTestHelper
import views.html.remove.RemoveConfirmationView

class RemoveConfirmationViewSpec extends ViewTestHelper {

  "view" should {

    "display correct text and guidance" in {
      val eori = "test_eori"
      val companyName = "test_company"

      val viewDoc: Document =
        Jsoup.parse(app.injector.instanceOf[RemoveConfirmationView].apply(eori, Some(companyName)).body)

      val pageElements = viewDoc.getElementsByClass("govuk-grid-column-two-thirds")
      implicit val elementsAsDoc: Document = Jsoup.parse(pageElements.html())

      titleShouldBeCorrect(viewDoc, titleMsgKey = "removeConfirmation.title")

      shouldContainCorrectConfirmationPanelGuidance(eori, companyName)

      shouldContainLinkToGoBackToAuthoritiesPage
    }
  }

  private def shouldContainCorrectConfirmationPanelGuidance(eori: String,
                                                            companyName: String)(implicit view: Document): Assertion = {
    view.getElementsByTag("h1").text() mustBe messages("removeConfirmation.authorityRemoved")
    view.html().contains(messages("removeConfirmation.companyName")) mustBe true
    view.html().contains(messages("removeConfirmation.eoriNumber")) mustBe true
    view.html().contains(eori) mustBe true
    view.html().contains(companyName) mustBe true
  }

  private def shouldContainLinkToGoBackToAuthoritiesPage(implicit view: Document): Assertion = {
    val linkTag: Elements = view.getElementsByTag("a")
    val anchorTag = linkTag.get(0).toString

    anchorTag.contains(controllers.routes.ManageAuthoritiesController.onPageLoad().url) mustBe true
    anchorTag.contains(messages("removeConfirmation.returnLink")) mustBe true
  }

}
