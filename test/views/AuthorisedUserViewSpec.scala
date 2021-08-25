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
import forms.AuthorisedUserFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Mockito.when
import org.scalatest.Matchers._
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeRequest, Helpers}
import services.DateTimeService
import viewmodels.CheckYourAnswersHelper
import views.html.add.AuthorisedUserView

import java.time.LocalDateTime

class AuthorisedUserViewSpec extends SpecBase with MockitoSugar {


  "AuthorisedUserView" should {
      "have back link" in new Setup {
        view().getElementById("back-link").attr("href") mustBe s"/customs/manage-authorities/add-authority/available-balance"
      }
    }


  trait Setup  {
    val mockDateTimeService: DateTimeService = mock[DateTimeService]
    when(mockDateTimeService.localTime()).thenReturn(LocalDateTime.now())

    implicit val csrfRequest: FakeRequest[AnyContentAsEmpty.type] = fakeRequest("GET", "/some/resource/path")
    val app: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
    implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
    implicit val messages: Messages = Helpers.stubMessages()

    val helper: CheckYourAnswersHelper = CheckYourAnswersHelper(emptyUserAnswers, mockDateTimeService)
    private val formProvider = new AuthorisedUserFormProvider()
    private val form = formProvider()
    def view(): Document = Jsoup.parse(app.injector.instanceOf[AuthorisedUserView].apply(form,helper).body)
  }
}
