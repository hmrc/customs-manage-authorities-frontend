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

import config.FrontendAppConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.ViewTestHelper
import viewmodels.AuthoritiesFilesNotificationViewModel
import views.html.components.authoritiesNotificationPanel

import scala.jdk.CollectionConverters.SetHasAsScala

class AuthoritiesNotificationPanelSpec extends ViewTestHelper {

  "AuthoritiesNotificationPanel" should {

    "display the correct guidance when only GB authority file exists" in new Setup {
      val viewModel      = AuthoritiesFilesNotificationViewModel(Option("gbURL"), None, date)
      val view: Document = Jsoup.parse(app.injector.instanceOf[authoritiesNotificationPanel].apply(viewModel).body)

      val elements: Elements = view.getElementsByClass("govuk-body")
      elements.size() mustBe 2

      elements.get(0).getElementsByTag("p").html() must contain
      messages(app)("manageAuthorities.notificationPanel.p1")

      elements.get(1).html() mustBe messages(app)("manageAuthorities.notificationPanel.p2", date)

      view.getElementById("gb-csv-authority-link").html() mustBe
        messages(app)("manageAuthorities.notificationPanel.a.gb-authority")
      view.getElementById("gb-csv-authority-link").attr("href") mustBe "gbURL"
    }

    "display the correct guidance when both GB and XI authorities' file exist" in new Setup {
      val viewModel      = AuthoritiesFilesNotificationViewModel(Option("gbURL"), Option("xiURL"), date)
      val view: Document = Jsoup.parse(app.injector.instanceOf[authoritiesNotificationPanel].apply(viewModel).body)

      val elements: Elements = view.getElementsByClass("govuk-body")
      elements.size() mustBe 2

      view.getElementById("gb-csv-authority-link").html() mustBe
        messages(app)("manageAuthorities.notificationPanel.a.gb-authority")
      view.getElementById("gb-csv-authority-link").attr("href") mustBe "gbURL"

      view.getElementById("xi-csv-authority-link").html() mustBe
        messages(app)("manageAuthorities.notificationPanel.a.xi-authority")
      view.getElementById("xi-csv-authority-link").attr("href") mustBe "xiURL"

      elements.get(0).getElementsByTag("p").html() must contain
      messages(app)("manageAuthorities.notificationPanel.p1")

      elements.get(1).html() mustBe messages(app)("manageAuthorities.notificationPanel.p2", date)
    }

    "not display the guidance when file does not exists" in new Setup {
      val viewModel      = AuthoritiesFilesNotificationViewModel(None, None, date)
      val view: Document = Jsoup.parse(app.injector.instanceOf[authoritiesNotificationPanel].apply(viewModel).body)

      Option(view.getElementById("notification-panel")) mustBe None

      view.getElementsByClass("govuk-body").size() mustBe 0
    }

    "apply the correct extra classes when provided" in new Setup {
      val viewModel      = AuthoritiesFilesNotificationViewModel(Option("gbURL"), None, date)
      val view: Document = Jsoup.parse(
        app.injector
          .instanceOf[authoritiesNotificationPanel]
          .apply(viewModel, extraClasses = Some("govuk-!-width-two-thirds"))
          .body
      )

      private val notificationPanel = view.getElementsByClass("notifications-panel").first()

      notificationPanel.hasClass("govuk-!-width-two-thirds") mustBe true
    }

    "not apply extra classes when none are provided" in new Setup {
      val viewModel      = AuthoritiesFilesNotificationViewModel(Option("gbURL"), None, date)
      val view: Document = Jsoup.parse(
        app.injector
          .instanceOf[authoritiesNotificationPanel]
          .apply(viewModel)
          .body
      )

      private val notificationPanel = view.getElementsByClass("notifications-panel").first()

      notificationPanel.classNames().asScala mustBe Set("notifications-panel", "govuk-!-margin-top-8")
    }
  }

  trait Setup {
    implicit val appConfig: FrontendAppConfig                 = app.injector.instanceOf[FrontendAppConfig]
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")

    val date = "25 November 2022"
  }
}
