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
import models.domain.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.BeforeAndAfterAll
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.servicenavigation.ServiceNavigationItem
import viewmodels.{AuthoritiesFilesNotificationViewModel, ManageAuthoritiesViewModel}
import views.html.AuthorisedAccountsView

import java.time.LocalDate
import scala.jdk.CollectionConverters.*

class AuthorisedAccountsViewSpec extends SpecBase with BeforeAndAfterAll {
  private lazy val app: Application = applicationBuilder().build()
  private lazy val appConfig = app.injector.instanceOf[FrontendAppConfig]
  private val authority = StandingAuthority("GB9988776655000", LocalDate.of(2020, 1, 1), None, viewBalance = false)
  private val accounts = Map(
    "guarantee" -> AccountWithAuthoritiesWithId(CdsGeneralGuaranteeAccount, "GAN-9876500", Some(AccountStatusClosed), None, Map("auth" -> authority)),
    "deferment" -> AccountWithAuthoritiesWithId(CdsDutyDefermentAccount, "DAN-1234500", Some(AccountStatusOpen), None,
      Map("auth" -> authority, "second" -> authority.copy(authorisedEori = "XI9988776655000", authorisedFromDate = LocalDate.of(2021, 1, 1)))),
    "cash" -> AccountWithAuthoritiesWithId(CdsCashAccount, "223344556", Some(AccountStatusOpen), None, Map("auth" -> authority))
  )

  override def afterAll(): Unit = {
    app.stop().futureValue
    super.afterAll()
  }

  private def render(language: String): Document = {
    implicit val msgs: Messages = MessagesImpl(Lang(language), app.injector.instanceOf[MessagesApi])
    val model = ManageAuthoritiesViewModel(AuthoritiesWithId(accounts), CDSAccounts("GB123456789012", Nil),
      Map(authority.authorisedEori -> "Tony Stark"), AuthoritiesFilesNotificationViewModel(None, None, ""))
    val nav = Some(Seq(ServiceNavigationItem(href = "/home", content = Text("Home"))))
    Jsoup.parse(app.injector.instanceOf[AuthorisedAccountsView].apply(model, nav)(fakeRequest(), msgs, appConfig).body)
  }

  "Authorised accounts page" should {
    for (language <- Seq("en", "cy")) {
      s"display the title, introduction, existing authorities and navigation in $language" in {
        val msgs: Messages = MessagesImpl(Lang(language), app.injector.instanceOf[MessagesApi])
        val doc = render(language)
        doc.title must include(msgs("manageAuthorities.accounts.authorised"))
        doc.select("main h1").text mustBe msgs("manageAuthorities.accounts.authorised")
        doc.select("main p").first.text mustBe msgs("authorisedAccounts.description")
        doc.select(".govuk-back-link").attr("href") mustBe controllers.routes.ManageAuthoritiesController.onPageLoad().url
        doc.select(".govuk-service-navigation__link").text must include("Home")
        doc.select("#accounts-with-authorities h3").asScala.map(_.text).toSeq mustBe Seq(
          msgs("manageAuthorities.table.heading.account.CdsCashAccount", "223344556"),
          msgs("manageAuthorities.table.heading.account.CdsDutyDefermentAccount", "DAN-1234500"),
          msgs("manageAuthorities.table.heading.account.CdsGeneralGuaranteeAccount.closed", "GAN-9876500"))
        doc.select("#accounts-with-authorities .govuk-summary-list__row").size mustBe 4
        doc.select("#CdsDutyDefermentAccount-DAN-1234500 dt").first.text must include(authority.authorisedEori)
        doc.select("#CdsDutyDefermentAccount-DAN-1234500 dt").last.text must include("XI9988776655000")
        doc.select("#CdsCashAccount-223344556 dt").text must include("Tony Stark")
        doc.select("#CdsCashAccount-223344556 a").attr("href") mustBe
          controllers.routes.ViewAuthorityController.onPageLoad("cash", "auth").url
        doc.select("#CdsGeneralGuaranteeAccount-GAN-9876500 a").size mustBe 0
        doc.select("#notification-panel, #start-link, #authorised-to-view-link").size mustBe 0
      }
    }
  }
}
