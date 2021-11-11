/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.AuthorityEndDateFormProvider
import models.{CheckMode, NormalMode}
import org.jsoup.Jsoup
import org.mockito.Mockito.when
import org.scalatest.Matchers._
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.{FakeRequest, Helpers}
import services.DateTimeService
import views.html.add.AuthorityEndDateView

import java.time.{LocalDate, LocalDateTime}

class AuthorityEndDateViewSpec extends SpecBase with MockitoSugar {


  "AuthorityEndDaterView" should {
      "when back-link is clicked returns to previous page on Normal Mode" in new Setup {
        normalModeView().getElementById("back-link").attr("href") mustBe s"/customs/manage-authorities/add-authority/end"
      }

    "when back-link is clicked returns to previous page on Check Mode" in new Setup {
      checkModeView().getElementById("back-link").attr("href") mustBe s"/customs/manage-authorities/add-authority/check-answers"
    }
    }


  trait Setup  {
    val mockDateTimeService = mock[DateTimeService]
    when(mockDateTimeService.localTime()).thenReturn(LocalDateTime.now())

    implicit val csrfRequest: FakeRequest[AnyContentAsEmpty.type] = fakeRequest("GET", "/some/resource/path")
    val app = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
    implicit val appConfig = app.injector.instanceOf[FrontendAppConfig]
    implicit val messages: Messages = Helpers.stubMessages()
    private lazy val normalModeBackLinkRoute: Call = controllers.add.routes.AuthorityEndController.onPageLoad(NormalMode)
    private lazy val checkModeBackLinkRoute: Call = controllers.add.routes.AuthorisedUserController.onPageLoad()


    private val formProvider = new AuthorityEndDateFormProvider(mockDateTimeService)
    private def form = formProvider(LocalDate.now())

    def normalModeView() = Jsoup.parse(app.injector.instanceOf[AuthorityEndDateView].apply(form,NormalMode,normalModeBackLinkRoute).body)
    def checkModeView() = Jsoup.parse(app.injector.instanceOf[AuthorityEndDateView].apply(form,CheckMode,checkModeBackLinkRoute).body)
  }
}
