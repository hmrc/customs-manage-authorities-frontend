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

package views.remove

import java.time.LocalDate
import base.SpecBase
import config.FrontendAppConfig
import connectors.CustomsDataStoreConnector
import models.UserAnswers
import models.domain.{AccountStatusOpen, AccountWithAuthoritiesWithId, AuthorisedUser, CdsCashAccount, StandingAuthority}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.remove.RemoveAuthorisedUserPage
import play.api.i18n.Messages
import play.api.{Application, inject}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.CheckYourAnswersRemoveHelper
import views.html.remove.RemoveCheckYourAnswersView

import scala.concurrent.Future

class RemoveCheckYourAnswersViewSpec extends SpecBase with MockitoSugar {

  "RemoveCheckYourAnswersView" should {

    "display correct elements in right order" in new Setup {
      val doc: Document = Jsoup.parse(requestView.toString())

      val summaryElements: Elements = doc.getElementsByClass(classSummaryListRow)
      summaryElements.size() must be > 2

      val companyDetailsRowEoriNumberElement: Element = summaryElements.get(0)
      val companyDetailsRowCompanyNameElement: Element = summaryElements.get(1)
      val yourAccountRowElement: Element = summaryElements.get(2)

      companyDetailsRowEoriNumberElement.getElementsByClass(
        classSummaryListKey).html() mustBe msgs("checkYourAnswers.eoriNumber.label")

      companyDetailsRowEoriNumberElement.getElementsByClass(
        classSummaryListValue).html() mustBe eori

      companyDetailsRowCompanyNameElement.getElementsByClass(
        classSummaryListKey).html() mustBe msgs("view-authority-h2.5")

      companyDetailsRowCompanyNameElement.getElementsByClass(
        classSummaryListValue).html() mustBe companyName

      yourAccountRowElement.getElementsByClass(
        classSummaryListKey).html() mustBe msgs("edit-cya-account-number")

      yourAccountRowElement.getElementsByClass(
        classSummaryListValue).html() mustBe msgs(
        s"manageAuthorities.table.heading.account.${CdsCashAccount}", accNumber)
    }

    "Eori Number Label has correct Text" in new Setup {
      val doc: Document = Jsoup.parse(requestView.toString())

      val summaryElements: Elements = doc.getElementsByClass(classSummaryListRow)
      summaryElements.size() must be > 0

      val companyDetailsRowEoriNumberElement: Element = summaryElements.get(0)

      companyDetailsRowEoriNumberElement.getElementsByClass(
        classSummaryListKey).html() mustBe msgs("checkYourAnswers.eoriNumber.label")

      companyDetailsRowEoriNumberElement.getElementsByClass(
        classSummaryListValue).html() mustBe eori
    }

    "Company Name Row has correct Text" in new Setup {
      val doc: Document = Jsoup.parse(requestView.toString())

      val summaryElements: Elements = doc.getElementsByClass(classSummaryListRow)
      summaryElements.size() must be > 0

      val companyDetailsRowCompanyNameElement: Element = summaryElements.get(1)

      companyDetailsRowCompanyNameElement.getElementsByClass(
        classSummaryListKey).html() mustBe msgs("view-authority-h2.5")

      companyDetailsRowCompanyNameElement.getElementsByClass(
        classSummaryListValue).html() mustBe companyName
    }

    "Eori Numnber has correct Text" in new Setup {
      val doc: Document = Jsoup.parse(requestView.toString())

      val summaryElements: Elements = doc.getElementsByClass(classSummaryListRow)
      summaryElements.size() must be > 0

      val yourAccountRowElement: Element = summaryElements.get(2)

      yourAccountRowElement.getElementsByClass(classSummaryListKey).html() mustBe
        msgs("edit-cya-account-number")
      yourAccountRowElement.getElementsByClass(classSummaryListValue).html() mustBe
        msgs(s"manageAuthorities.table.heading.account.${CdsCashAccount}", accNumber)
    }

    "Header displays Check your answers by Tag" in new Setup {
      val doc: Document = Jsoup.parse(requestView.toString())
      val elements: Elements = doc.getElementsByTag("h1")

      elements.size() must be > 0
      elements.text() mustBe "Check your answers"
    }

    "Page ends in help to make better link" in new Setup {
      val doc: Document = Jsoup.parse(requestView.toString())
      val elements: Elements = doc.getElementsByTag("h2")

      elements.size() must be > 0
      elements.text() mustBe "Help make GOV.UK better Remove authority for company Account you have" +
        " authorised Your details Support links"
    }

    "Header displays Account authority" in new Setup {
      val doc: Document = Jsoup.parse(requestView.toString())

      val elements: Element = doc.getElementById("remove-cya-heading")
      elements.text() mustBe "Check your answers"
    }

    "Label 1 displays Remove authority for company" in new Setup {
      val doc: Document = Jsoup.parse(requestView.toString())

      val elements: Element = doc.getElementById("remove-cya-h2.1")
      elements.text() mustBe "Remove authority for company"
    }

    "Label 2 displays Your details" in new Setup {
      val doc: Document = Jsoup.parse(requestView.toString())

      val elements: Element = doc.getElementById("remove-cya-h2.2")
      elements.text() mustBe "Account you have authorised"
    }

    "Label 3 displays Your Details by Id" in new Setup {
      val doc: Document = Jsoup.parse(requestView.toString())

      val elements: Element = doc.getElementById("remove-cya-h2.3")
      elements.text() mustBe "Your details"
    }

    "display confirm and cancel button with correct text" in new Setup {
      val doc: Document = Jsoup.parse(requestView.toString())

      val divElementWithCancelAndConfirmButtons: Elements = doc.getElementsByClass("govuk-button-group")
      val elementsAsDoc: Document = Jsoup.parse(divElementWithCancelAndConfirmButtons.get(0).toString)

      val confirmButton: Elements = elementsAsDoc.getElementsByTag("button")
      val cancelButton: Elements = elementsAsDoc.getElementsByClass("govuk-visually-hidden")

      confirmButton.text() mustBe msgs("remove-cya-button")
      cancelButton.text() mustBe msgs("remove-cya-visually-hidden-cancel")
    }
  }

  trait Setup {
    val eori = "test_EORI"
    val companyName = "test_company"
    val accNumber = "12345"
    val accountId = "a"
    val authorityId = "b"

    val authUser: AuthorisedUser = AuthorisedUser("test", "test")

    val classSummaryListRow = "govuk-summary-list__row"
    val classSummaryListKey = "govuk-summary-list__key"
    val classSummaryListValue = "govuk-summary-list__value"

    val userAnswers: UserAnswers =
      emptyUserAnswers.set(RemoveAuthorisedUserPage(accountId, authorityId), authUser).get

    val startDate: LocalDate = LocalDate.parse("2020-03-01")
    val endDate: LocalDate = LocalDate.parse("2020-04-01")

    val standingAuthority: StandingAuthority = StandingAuthority(
      eori, startDate, Some(endDate), viewBalance = false)

    val accountsWithAuthoritiesWithId: AccountWithAuthoritiesWithId =
      AccountWithAuthoritiesWithId(
        CdsCashAccount,
        accNumber,
        Some(AccountStatusOpen),
        Map(authorityId -> standingAuthority))

    implicit val csrfRequest: FakeRequest[AnyContentAsEmpty.type] = fakeRequest(
      "GET", "/some/resource/path")

    val mockDataStoreConnector: CustomsDataStoreConnector = mock[CustomsDataStoreConnector]

    val app: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(
      inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector)).build()

    implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val msgs: Messages = messages(app)

    val view: RemoveCheckYourAnswersView = app.injector.instanceOf[RemoveCheckYourAnswersView]

    when(mockDataStoreConnector.getCompanyName(any[String])(any[HeaderCarrier]))
      .thenReturn(Future.successful(Some(companyName)))

    val helper = new CheckYourAnswersRemoveHelper(
      userAnswers,
      accountId,
      authorityId,
      authUser,
      standingAuthority,
      accountsWithAuthoritiesWithId,
      mockDataStoreConnector)

    val requestView: HtmlFormat.Appendable = view(helper)
  }
}
