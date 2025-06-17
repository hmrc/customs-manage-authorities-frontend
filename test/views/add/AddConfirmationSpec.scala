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

package views.add

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalatest.Assertion
import utils.ViewTestHelper
import views.html.add.AddConfirmationView

class AddConfirmationViewSpec extends ViewTestHelper {

  "view" should {
    "display correct text and guidance" in {
      val eori        = "test_eori"
      val companyName = "test_company"

      val viewDoc: Document =
        Jsoup.parse(app.injector.instanceOf[AddConfirmationView].apply(eori, None, Some(companyName), false).body)

      val pageElements                     = viewDoc.getElementsByClass("govuk-grid-column-two-thirds")
      implicit val elementsAsDoc: Document = Jsoup.parse(pageElements.html())

      shouldContainCorrectRecruitmentDetails
    }
  }

  private def shouldContainCorrectRecruitmentDetails(implicit view: Document): Assertion = {
    view.html().contains(messages("user-research.subheader-text")) mustBe true
    view.html().contains(messages("user-research.help.body-text")) mustBe true
    view.html().contains(messages("user-research.help.link")) mustBe true
  }
}
