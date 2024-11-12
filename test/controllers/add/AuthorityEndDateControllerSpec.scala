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

package controllers.add

import base.SpecBase
import config.FrontendAppConfig
import forms.AuthorityEndDateFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.add.AuthorityEndDatePage
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, inject}
import repositories.SessionRepository
import services.DateTimeService
import views.html.add.AuthorityEndDateView

import java.time.{LocalDate, LocalDateTime, ZoneOffset}
import scala.concurrent.Future

class AuthorityEndDateControllerSpec extends SpecBase {

  "onPageLoad" must {

    "return OK with pre-populated values if form has some values" in new SetUp {
      when(mockDateTimeService.localTime()).thenReturn(LocalDateTime.now(ZoneOffset.UTC))

      val app: Application = applicationBuilder(emptyUserAnswers.set(
        AuthorityEndDatePage, LocalDate.now(ZoneOffset.UTC)).toOption).build()

      val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
      val form: AuthorityEndDateFormProvider = app.injector.instanceOf[AuthorityEndDateFormProvider]
      val view: AuthorityEndDateView = app.injector.instanceOf[AuthorityEndDateView]

      running(app) {
        val result = route(app, getRequest).value
        status(result) shouldBe OK

        contentAsString(result) mustBe view(form(LocalDate.now(ZoneOffset.UTC))(messages(app)).fill(
          LocalDate.now(ZoneOffset.UTC)),
          NormalMode,
          controllers.add.routes.AuthorityEndController.onPageLoad(NormalMode))(
          getRequest, messages(app), appConfig).toString()
      }
    }

    "return OK without pre-populated values if form has no values" in new SetUp {
      when(mockDateTimeService.localTime()).thenReturn(LocalDateTime.now(ZoneOffset.UTC))

      val app: Application = applicationBuilder(Option(emptyUserAnswers)).build()
      val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
      val form: AuthorityEndDateFormProvider = app.injector.instanceOf[AuthorityEndDateFormProvider]
      val view: AuthorityEndDateView = app.injector.instanceOf[AuthorityEndDateView]

      running(app) {
        val result = route(app, getRequest).value
        status(result) shouldBe OK

        contentAsString(result) mustBe view(form(LocalDate.now(ZoneOffset.UTC))(messages(app)),
          NormalMode,
          controllers.add.routes.AuthorityEndController.onPageLoad(NormalMode))(
          getRequest, messages(app), appConfig).toString()
      }
    }
  }

  "onSubmit" must {

    "redirect to next page if form has no error" in new SetUp {
      val year2023 = 2023
      val monthOfTheYear = 6
      val dayOfMonth = 12
      val hourOfDay = 2
      val minuteOfHour = 20

      when(mockDateTimeService.localTime()).thenReturn(
        LocalDateTime.of(year2023, monthOfTheYear, dayOfMonth, hourOfDay, minuteOfHour))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val app: Application = applicationBuilder(Some(emptyUserAnswers)).overrides(
        inject.bind[SessionRepository].toInstance(mockSessionRepository),
        inject.bind[DateTimeService].toInstance(mockDateTimeService)
      ).build()

      running(app) {
        val result = route(app, postRequest.withFormUrlEncodedBody(
          "value.day" -> "12", "value.month" -> "6", "value.year" -> "2023")).value
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.add.routes.ShowBalanceController.onPageLoad(NormalMode).url
      }
    }

    "return BAD_REQUEST if form has errors" in new SetUp {
      when(mockDateTimeService.localTime()).thenReturn(LocalDateTime.now(ZoneOffset.UTC))

      val app: Application = applicationBuilder(Some(emptyUserAnswers)).build()

      running(app) {
        val result = route(app, postRequest.withFormUrlEncodedBody("invalid" -> "field_value")).value
        status(result) shouldBe BAD_REQUEST
      }
    }
  }

  trait SetUp {
    val mockSessionRepository: SessionRepository = mock[SessionRepository]
    val mockDateTimeService: DateTimeService = mock[DateTimeService]

    val getRequest: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET,
        controllers.add.routes.AuthorityEndDateController.onPageLoad(NormalMode).url)

    val postRequest: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(POST,
        controllers.add.routes.AuthorityEndDateController.onSubmit(NormalMode).url)
  }
}
