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
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.StringUtils.emptyString
import viewmodels.AuthoritiesFilesNotificationViewModel
import views.html.ManageAuthoritiesApiFailureView

class ManageAuthoritiesApiFailureViewSpec extends SpecBase {

  "ManageAuthoritiesApiFailureView" should {

    "display the correct page title" in new Setup {
      view().title must include(messages("manageAuthorities.title"))
    }

    "display the main heading" in new Setup {
      view().getElementById("manageAuthorities.heading").text mustBe messages("manageAuthorities.title")
    }

    "display the authorized-to-view link" in new Setup {
      private val link = view().getElementById("manageAuthorities-noAccounts-link")

      link.attr("href") mustBe appConfig.authorizedToViewUrl
      link.text mustBe messages("cf.account.authorized-to-view.title")
    }

    "display the notification panel if provided" in new Setup {
      override val viewModel: AuthoritiesFilesNotificationViewModel = AuthoritiesFilesNotificationViewModel(
        gbAuthUrl = Some("https://example.com/GBFile.csv"),
        xiAuthUrl = Some("https://example.com/XIFile.csv"),
        date = "01 January 2023"
      )

      view().select("div.notifications-panel").size mustBe 1
    }

    "not display the notification panel if no files provided" in new Setup {
      override val viewModel: AuthoritiesFilesNotificationViewModel =
        AuthoritiesFilesNotificationViewModel(None, None, date = "01 January 2023")

      view().getElementsByClass("notifications-panel") mustBe empty
    }

    "display the authorized accounts heading" in new Setup {
      view().select("h2.govuk-heading-m").first.text mustBe messages("manageAuthorities.accounts.authorised")
    }

    "display the error message paragraph" in new Setup {
      view().select("p.govuk-body").get(1).text mustBe messages("manageAuthorities.error.display")
    }
  }

  trait Setup {
    implicit val csrfRequest: FakeRequest[AnyContentAsEmpty.type] = fakeRequest("GET", "/some/resource/path")

    val app: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

    implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

    val viewModel: AuthoritiesFilesNotificationViewModel = AuthoritiesFilesNotificationViewModel(
      gbAuthUrl = None,
      xiAuthUrl = None,
      date = emptyString
    )

    def view(): Document = {
      val htmlContent = app.injector
        .instanceOf[ManageAuthoritiesApiFailureView]
        .apply(viewModel)(csrfRequest, messages, appConfig)
        .body
      Jsoup.parse(htmlContent)
    }
  }
}
