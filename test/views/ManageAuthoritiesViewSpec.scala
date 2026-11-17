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
import models.NormalMode
import models.domain.{AuthoritiesWithId, CDSAccounts}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.BeforeAndAfterAll
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.servicenavigation.ServiceNavigationItem
import utils.TestData.OPEN_CASH_ACC_WITH_AUTH_WITH_ID
import viewmodels.{AuthoritiesFilesNotificationViewModel, ManageAuthoritiesViewModel}
import views.html.ManageAuthoritiesView

import scala.jdk.CollectionConverters.*

class ManageAuthoritiesViewSpec extends SpecBase with BeforeAndAfterAll {
  private lazy val app: Application = applicationBuilder().build()
  private lazy val appConfig        = app.injector.instanceOf[FrontendAppConfig]
  private val csvUrl                = "https://example.com/authorities.csv"

  override def afterAll(): Unit = {
    app.stop().futureValue
    super.afterAll()
  }

  private def render(
    hasAuthorities: Boolean,
    gbUrl: Option[String],
    xiUrl: Option[String],
    language: String
  ): Document = {
    implicit val msgs: Messages = MessagesImpl(Lang(language), app.injector.instanceOf[MessagesApi])
    val authorities             =
      if (hasAuthorities) AuthoritiesWithId(Map("cash" -> OPEN_CASH_ACC_WITH_AUTH_WITH_ID))
      else AuthoritiesWithId(Map.empty)
    val model                   = ManageAuthoritiesViewModel(
      authorities,
      CDSAccounts("GB123456789012", Nil),
      filesNotificationViewModel = AuthoritiesFilesNotificationViewModel(gbUrl, xiUrl, "25 November 2022")
    )
    val navigation              = Some(Seq(ServiceNavigationItem(href = "/home", content = Text("Home"))))
    Jsoup.parse(
      app.injector
        .instanceOf[ManageAuthoritiesView]
        .apply(model, navigation)(fakeRequest(), msgs, appConfig)
        .body
    )
  }

  "Manage authorities landing page" should {
    for {
      language       <- Seq("en", "cy")
      hasAuthorities <- Seq(false, true)
      hasCsv         <- Seq(false, true)
    }
      s"display the correct variant with authorities=$hasAuthorities and CSV=$hasCsv in $language" in {
        val msgs: Messages = MessagesImpl(Lang(language), app.injector.instanceOf[MessagesApi])
        val doc            = render(hasAuthorities, Option.when(hasCsv)(csvUrl), None, language)
        doc.title                                          must include(msgs("manageAuthorities.title"))
        doc.select("main h1").text mustBe msgs("manageAuthorities.title")
        doc.select("main section h2").asScala.map(_.text).toSeq mustBe Seq(
          msgs("manageAuthorities.addAuthority"),
          msgs("manageAuthorities.accounts.authorised"),
          msgs("cf.account.authorized-to-view.title")
        )
        doc.select("#start-link").attr("href") mustBe controllers.add.routes.EoriNumberController
          .onPageLoad(NormalMode)
          .url
        doc.select("#authorised-to-view-link").attr("href") mustBe appConfig.authorityToUseUrl
        doc.select("#accounts-authorised-link").size mustBe (if (hasAuthorities) 1 else 0)
        if (hasAuthorities) {
          doc.select("#accounts-authorised-link").attr("href") mustBe
            controllers.routes.ManageAuthoritiesController.onAuthorisedAccountsPageLoad().url
          doc.select("#existing-authorities-heading + p").text mustBe msgs(
            "manageAuthorities.accounts.authorised.description"
          )
        } else {
          doc.select("#no-authorities").text mustBe msgs("manageAuthorities.notification.noAuthorities")
        }
        doc.select("#notification-panel").size mustBe (if (hasCsv) 1 else 0)
        if (hasCsv) {
          doc.select("#gb-csv-authority-link").attr("href") mustBe csvUrl
          doc.select("#notification-panel p").last.text mustBe msgs(
            "manageAuthorities.notificationPanel.p2",
            "25 November 2022"
          )
          doc.select("main #notification-panel, main section").first.id mustBe "notification-panel"
        }
        doc.select("#accounts-with-authorities").size mustBe 0
        doc.select(".govuk-grid-column-two-thirds").size mustBe 1
        doc.select(".govuk-back-link").attr("href") mustBe appConfig.customsFinancialsFrontendHomepageUrl
        doc.select(".govuk-service-navigation__link").text must include("Home")
        doc.select("#add-authority-heading + p").text mustBe msgs("manageAuthorities.addAuthority.description")
        doc.select("#find-authorities-heading + p").text mustBe msgs("manageAuthorities.findAccounts.description")
      }

    "retain GB and EU downloads when both are available" in {
      val doc = render(true, Some(csvUrl), Some("https://example.com/eu.csv"), "en")
      doc.select("#gb-csv-authority-link").text mustBe "GB authorities (CSV)"
      doc.select("#xi-csv-authority-link").text mustBe "EU authorities (CSV)"
      doc.select("#xi-csv-authority-link").attr("href") mustBe "https://example.com/eu.csv"
    }

    "show the notification when only an EU download is available" in {
      val doc = render(false, None, Some(csvUrl), "cy")
      doc.select("#notification-panel").size mustBe 1
      doc.select("#gb-csv-authority-link").size mustBe 0
      doc.select("#xi-csv-authority-link").text mustBe "Awdurdodau yn yr UE (CSV)"
      doc.select("#xi-csv-authority-link").attr("href") mustBe csvUrl
    }
  }
}
