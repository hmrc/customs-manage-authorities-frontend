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

import base.SpecBase
import config.FrontendAppConfig
import forms.EoriNumberFormProvider
import models.{CheckMode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.{FakeRequest, Helpers}
import services.DateTimeService
import views.html.add.EoriNumberView

import java.time.LocalDateTime

class EoriNumberViewSpec extends SpecBase with MockitoSugar {

  "EoriNumberView" should {
    "display the correct guidance in Normal mode with XI Eori Enabled" in new Setup {
      normalModeView().title() mustBe "eoriNumber.title - service.name - site.govuk"
      normalModeView().getElementById("value-hint-title").html() mustBe "eoriNumber.details.label"
      normalModeView().getElementsByClass("govuk-body").html() mustBe "eoriNumber.details.text"

      val detailsHintElement: Element = normalModeView().getElementById("value-hint-details")
      val hintLabelElement: Elements  = normalModeView().getElementsByClass("govuk-!-margin-top-6")

      detailsHintElement.getElementById("value-hint-title").html() mustBe "eoriNumber.details.label"
      detailsHintElement.getElementsByClass("govuk-body").html() mustBe "eoriNumber.details.text"

      hintLabelElement.html() mustBe "eoriNumber.hint.xi"

      normalModeView().getElementsByTag("button").get(1).html() mustBe "site.saveAndContinue"
    }

    "display the correct guidance in Check mode with XI Eori Enabled" in new Setup {
      checkModeView().title() mustBe "eoriNumber.title - service.name - site.govuk"
      checkModeView().getElementById("value-hint-title").html() mustBe "eoriNumber.details.label"
      checkModeView().getElementsByClass("govuk-body").html() mustBe "eoriNumber.details.text"

      val detailsHintElement: Element = normalModeView().getElementById("value-hint-details")
      val hintLabelElement: Elements  = normalModeView().getElementsByClass("govuk-!-margin-top-6")

      detailsHintElement.getElementById("value-hint-title").html() mustBe "eoriNumber.details.label"
      detailsHintElement.getElementsByClass("govuk-body").html() mustBe "eoriNumber.details.text"

      hintLabelElement.html() mustBe "eoriNumber.hint.xi"

      normalModeView().getElementsByTag("button").get(1).html() mustBe "site.saveAndContinue"
    }

    "display the correct guidance in Normal mode with EU Eori Enabled" in new Setup {
      override val xiEoriEnabled = false
      override val euEoriEnabled = true

      override def normalModeView(): Document = Jsoup.parse(
        app.injector
          .instanceOf[EoriNumberView]
          .apply(form, NormalMode, normalModeBackLinkRoute, xiEoriEnabled, euEoriEnabled)
          .body
      )
      override def checkModeView(): Document  = Jsoup.parse(
        app.injector
          .instanceOf[EoriNumberView]
          .apply(form, CheckMode, checkModeBackLinkRoute, xiEoriEnabled, euEoriEnabled)
          .body
      )

      normalModeView().getElementById("value-hint-title").html() mustBe "eoriNumber.details.label"
      normalModeView().getElementsByClass("govuk-body").html() mustBe "eoriNumber.details.text.eu"
      val hintLabelElement: Elements = normalModeView().getElementsByClass("govuk-!-margin-top-6")
      hintLabelElement.html() mustBe "eoriNumber.hint.eu"
    }

    "display the correct guidance in Normal mode with all Eori Flags Disabled" in new Setup {
      override val xiEoriEnabled = false
      override val euEoriEnabled = false

      override def normalModeView(): Document = Jsoup.parse(
        app.injector
          .instanceOf[EoriNumberView]
          .apply(form, NormalMode, normalModeBackLinkRoute, xiEoriEnabled, euEoriEnabled)
          .body
      )
      override def checkModeView(): Document  = Jsoup.parse(
        app.injector
          .instanceOf[EoriNumberView]
          .apply(form, CheckMode, checkModeBackLinkRoute, xiEoriEnabled, euEoriEnabled)
          .body
      )

      normalModeView().getElementById("value-hint-title").html() mustBe "eoriNumber.details.label"
      normalModeView().getElementsByClass("govuk-body").html() mustBe "eoriNumber.details.text"
      val hintLabelElement: Elements = normalModeView().getElementsByClass("govuk-!-margin-top-6")
      hintLabelElement.html() mustBe "eoriNumber.hint"
    }

    "when back-link is clicked returns to previous page on Normal Mode" in new Setup {
      normalModeView().getElementsByClass("govuk-back-link").attr("href") mustBe
        s"/customs/manage-authorities/manage-account-authorities"
    }

    "when back-link is clicked returns to previous page on Check Mode" in new Setup {
      checkModeView().getElementsByClass("govuk-back-link").attr("href") mustBe
        s"/customs/manage-authorities/add-authority/check-answers"
    }
  }

  trait Setup {
    val mockDateTimeService: DateTimeService = mock[DateTimeService]
    when(mockDateTimeService.localTime()).thenReturn(LocalDateTime.now())

    implicit val csrfRequest: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest("GET", "/some/resource/path")
    val app: Application                                          = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
    implicit val appConfig: FrontendAppConfig                     = app.injector.instanceOf[FrontendAppConfig]
    implicit val messages: Messages                               = Helpers.stubMessages()

    private val formProvider = new EoriNumberFormProvider(appConfig)
    val form                 = formProvider()

    lazy val normalModeBackLinkRoute: Call = controllers.routes.ManageAuthoritiesController.onPageLoad()
    lazy val checkModeBackLinkRoute: Call  = controllers.add.routes.AuthorisedUserController.onPageLoad()

    val xiEoriEnabled: Boolean = true
    val euEoriEnabled: Boolean = false

    def normalModeView(): Document = Jsoup.parse(
      app.injector
        .instanceOf[EoriNumberView]
        .apply(form, NormalMode, normalModeBackLinkRoute, xiEoriEnabled, euEoriEnabled)
        .body
    )
    def checkModeView(): Document  = Jsoup.parse(
      app.injector
        .instanceOf[EoriNumberView]
        .apply(form, CheckMode, checkModeBackLinkRoute, xiEoriEnabled, euEoriEnabled)
        .body
    )
  }
}
