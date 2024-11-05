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

package views

import base.SpecBase
import config.FrontendAppConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeRequest, Helpers}
import views.html.ManageAuthoritiesView
import viewmodels.ManageAuthoritiesViewModel
import play.twirl.api.HtmlFormat
import models.domain.{AuthoritiesWithId, CDSAccounts}

class ManageAuthoritiesViewSpec extends SpecBase {

  "ManageAuthoritiesView" should {
    "display the correct page title" in new Setup {
      view().title must include(messages("manageAuthorities.title"))
    }

    "display the heading title" in new Setup {
      view().getElementById("manageAuthorities.heading").text mustBe messages("manageAuthorities.title")
    }

    "display notifications bar if provided" in new Setup {
      override val maybeMessageBannerPartial: Option[HtmlFormat.Appendable] = Some(
        HtmlFormat.raw(
            """
            <div class='notifications-bar'>Content</div>
            """
        )
      )
      view().select("div.notifications-bar").text mustBe "Content"
    }

  }

  trait Setup {
    implicit val csrfRequest: FakeRequest[AnyContentAsEmpty.type] = fakeRequest("GET", "/some/resource/path")

    val app: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

    implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
    implicit val messages: Messages = Helpers.stubMessages()

    val viewModel: ManageAuthoritiesViewModel = ManageAuthoritiesViewModel(
      authorities = AuthoritiesWithId(Nil),
      accounts = CDSAccounts("testEori", List.empty),
      auhorisedEoriAndCompanyMap = Map.empty
    )
    val maybeMessageBannerPartial: Option[HtmlFormat.Appendable] = None

    def view(): Document = {
      val htmlContent = app.injector.instanceOf[ManageAuthoritiesView]
        .apply(viewModel, maybeMessageBannerPartial)(csrfRequest, messages, appConfig)
        .body
      Jsoup.parse(htmlContent)
    }
  }
}
