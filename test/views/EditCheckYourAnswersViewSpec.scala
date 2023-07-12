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

package views

import base.SpecBase
import config.FrontendAppConfig
import models.domain._
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.scalatestplus.mockito.MockitoSugar
import pages.remove.RemoveAuthorisedUserPage
import play.api.{Application, inject}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import services.DateTimeService
import viewmodels.CheckYourAnswersEditHelper
import views.html.edit.EditCheckYourAnswersView
import java.time.LocalDate

class EditCheckYourAnswersViewSpec extends SpecBase with MockitoSugar {
  "view" should {
    "show correct elements with right order" in new Setup {

      val doc: Document = Jsoup.parse(result.toString())
      val summaryElements: Elements = doc.getElementsByClass(classSummaryListRow)
      summaryElements.size() must be >= 2

      val companyDetailsRowEoriNumberElement: Element = summaryElements.get(0)
      val companyDetailsRowCompanyNameElement: Element = summaryElements.get(1)
      val yourAccountRowElement: Element = summaryElements.get(2)

      // Order of the summary rows is also being tested
      companyDetailsRowEoriNumberElement.getElementsByClass(
        classSummaryListKey).html() mustBe messages(app)("checkYourAnswers.eoriNumber.label")

      companyDetailsRowEoriNumberElement.getElementsByClass(
        classSummaryListValue).html() mustBe eori

      companyDetailsRowCompanyNameElement.getElementsByClass(
        classSummaryListKey).html() mustBe messages(app)("view-authority-h2.5")

      companyDetailsRowCompanyNameElement.getElementsByClass(
        classSummaryListValue).html() mustBe companyName

      yourAccountRowElement.getElementsByClass(
        classSummaryListKey).html() mustBe messages(app)("edit-cya-account-number")

      yourAccountRowElement.getElementsByClass(
        classSummaryListValue).html() mustBe messages(app)(
        s"manageAuthorities.table.heading.account.${CdsCashAccount}", accNumber)
    }

    "Eori Number Label is correct Text" in new Setup {
      val doc: Document = Jsoup.parse(result.toString())
      val summaryElements: Elements = doc.getElementsByClass(classSummaryListRow)
      summaryElements.size() must be > 0

      val companyDetailsRowEoriNumberElement: Element = summaryElements.get(0)

      companyDetailsRowEoriNumberElement.getElementsByClass(
        classSummaryListKey).html() mustBe messages(app)("checkYourAnswers.eoriNumber.label")

      companyDetailsRowEoriNumberElement.getElementsByClass(
        classSummaryListValue).html() mustBe eori
    }

    "Company Name Row is correct Text" in new Setup {
      val doc: Document = Jsoup.parse(result.toString())
      val summaryElements: Elements = doc.getElementsByClass(classSummaryListRow)
      summaryElements.size() must be > 0

      val companyDetailsRowCompanyNameElement: Element = summaryElements.get(1)

      companyDetailsRowCompanyNameElement.getElementsByClass(
        classSummaryListKey).html() mustBe messages(app)("view-authority-h2.5")

      companyDetailsRowCompanyNameElement.getElementsByClass(
        classSummaryListValue).html() mustBe companyName
    }

    "Eori Numnber is correct Text" in new Setup {
      val doc: Document = Jsoup.parse(result.toString())
      val summaryElements: Elements = doc.getElementsByClass(classSummaryListRow)
      summaryElements.size() must be > 0

      val yourAccountRowElement: Element = summaryElements.get(2)

      yourAccountRowElement.getElementsByClass(
        classSummaryListKey).html() mustBe messages(app)("edit-cya-account-number")

      yourAccountRowElement.getElementsByClass(
        classSummaryListValue).html() mustBe messages(app)(
        s"manageAuthorities.table.heading.account.${CdsCashAccount}", accNumber)
    }

    "Header is Check your answers" in new Setup {
      val doc: Document = Jsoup.parse(result.toString())
      val elements: Elements = doc.getElementsByTag("h1")
      elements.size() must be > 0
      elements.text() mustBe "Check your answers"
    }

    "Page ends in help to make better link" in new Setup {
      val doc: Document = Jsoup.parse(result.toString())
      val elements: Elements = doc.getElementsByTag("h2")
      elements.size() must be > 0
      elements.text() mustBe "Help make GOV.UK better Authorised company Account you have authorised Authority details Your details Support links"
    }
  }

  trait Setup {
    val eori = "test_EORI"
    val companyName = "test_company"
    val accNumber = "12345"

    val classSummaryListRow = "govuk-summary-list__row"
    val classSummaryListKey = "govuk-summary-list__key"
    val classSummaryListValue = "govuk-summary-list__value"

    val userAnswers = emptyUserAnswers.set(RemoveAuthorisedUserPage("a", "b"), AuthorisedUser("test", "test")).get

    val startDate: LocalDate = LocalDate.parse("2020-03-01")
    val endDate: LocalDate = LocalDate.parse("2020-04-01")

    val standingAuthority: StandingAuthority = StandingAuthority(eori, startDate, Some(endDate), viewBalance = false)
    val accountsWithAuthoritiesWithId: AccountWithAuthoritiesWithId =
      AccountWithAuthoritiesWithId(CdsCashAccount, accNumber, Some(AccountStatusOpen), Map("b" -> standingAuthority))

    implicit val csrfRequest: FakeRequest[AnyContentAsEmpty.type] = fakeRequest(
      "GET", "/some/resource/path")

    val mockDateTimeService: DateTimeService = mock[DateTimeService]

    val app: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(
      inject.bind[DateTimeService].toInstance(mockDateTimeService)).build()

    implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

    val checkAnswersEditHelper = new CheckYourAnswersEditHelper(userAnswers,
      "123", "456", mockDateTimeService, standingAuthority,
      accountsWithAuthoritiesWithId, Option(companyName))(messages(app))

    val view: EditCheckYourAnswersView = app.injector.instanceOf[EditCheckYourAnswersView]

    val result: HtmlFormat.Appendable = view(
      checkAnswersEditHelper, "123", "456")(csrfRequest, messages(app), appConfig)
  }
}
