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
import models.domain.{AuthoritiesWithId, CDSAccounts}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeRequest, Helpers}
import utils.TestData.START_DATE_1
import viewmodels.ManageAuthoritiesViewModel.dateAsDayMonthAndYear
import viewmodels.{AuthoritiesFilesNotificationViewModel, ManageAuthoritiesViewModel}
import views.html.ManageAuthoritiesView
import uk.gov.hmrc.govukfrontend.views.viewmodels.servicenavigation.ServiceNavigationItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import models.withNameToString
import org.jsoup.select.Elements

import java.util

class ManageAuthoritiesViewSpec extends SpecBase {

  "ManageAuthoritiesView" should {
    "display the correct page title" in new Setup {
      view().title must include(messages("manageAuthorities.title"))
    }

    "display the heading title" in new Setup {
      view().getElementById("manageAuthorities.heading").text mustBe messages("manageAuthorities.title")
    }

    "display the notification panel if files provided" in new Setup {
      view().select("div.notifications-panel").size mustBe 1
    }

    "not display the notification panel if no files provided" in new Setup {
      override val standingAuthorityFilesViewModel: AuthoritiesFilesNotificationViewModel =
        AuthoritiesFilesNotificationViewModel(None, None, dateAsDayMonthAndYear(START_DATE_1))

      view().getElementsByClass("notifications-panel") mustBe empty
    }

    "display service navigation if provided" in new Setup {
      override val maybeMessageBannerPartial: Option[Seq[ServiceNavigationItem]] = Some(
        Seq(
          ServiceNavigationItem(
            href = Some("/test"),
            content = Text("Content")
          )
        )
      )

      val nav = view().select("nav.govuk-service-navigation__wrapper")
      nav.size mustBe 1

      view().select(".govuk-service-navigation__link").text must include("Content")
    }

  }

  trait Setup {
    implicit val csrfRequest: FakeRequest[AnyContentAsEmpty.type] = fakeRequest("GET", "/some/resource/path")

    val app: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

    implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
    implicit val messages: Messages           = Helpers.stubMessages()

    val gbStanAuthFile154Url = "https://test.co.uk/GB123456789012/SA_000000000154_csv.csv"
    val xiStanAuthFile154Url = "https://test.co.uk/XI123456789012/SA_000000000154_XI_csv.csv"

    val standingAuthorityFilesViewModel = AuthoritiesFilesNotificationViewModel(
      Some(gbStanAuthFile154Url),
      Some(xiStanAuthFile154Url),
      dateAsDayMonthAndYear(START_DATE_1)
    )

    lazy val viewModel: ManageAuthoritiesViewModel = ManageAuthoritiesViewModel(
      authorities = AuthoritiesWithId(Nil),
      accounts = CDSAccounts("testEori", List.empty),
      auhorisedEoriAndCompanyMap = Map.empty,
      filesNotificationViewModel = standingAuthorityFilesViewModel
    )

    val maybeMessageBannerPartial: Option[Seq[ServiceNavigationItem]] = None

    def view(): Document = {
      val htmlContent = app.injector
        .instanceOf[ManageAuthoritiesView]
        .apply(viewModel, maybeMessageBannerPartial)(csrfRequest, messages, appConfig)
        .body
      Jsoup.parse(htmlContent)
    }
  }
}
