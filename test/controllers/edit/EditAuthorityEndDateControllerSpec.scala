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

package controllers.edit

import base.SpecBase
import forms.AuthorityEndDateFormProvider
import models.UserAnswers
import navigation.{FakeNavigator, Navigator}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.edit.EditAuthorityEndDatePage
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers._
import repositories.SessionRepository
import services.DateTimeService
import views.html.edit.EditAuthorityEndDateView

import java.time.{LocalDate, LocalDateTime, ZoneOffset}
import scala.concurrent.Future

class EditAuthorityEndDateControllerSpec extends SpecBase with MockitoSugar {

  implicit val messages: Messages = Helpers.stubMessages()
  val mockDateTimeService = mock[DateTimeService]
  when(mockDateTimeService.localTime()).thenReturn(LocalDateTime.now())

  private val formProvider = new AuthorityEndDateFormProvider(mockDateTimeService)
  private def form = formProvider(LocalDate.now())

  private def onwardRoute = Call("GET", "/foo")

  private val validAnswer = LocalDate.now(ZoneOffset.UTC).plusDays(1)

  private lazy val authorityEndDateRoute = controllers.edit.routes.EditAuthorityEndDateController.onPageLoad("someId", "someId").url

  override val emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  private lazy val getRequest: FakeRequest[AnyContentAsEmpty.type] =
    fakeRequest(GET, authorityEndDateRoute)

  private lazy val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
    fakeRequest(POST, authorityEndDateRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year"  -> validAnswer.getYear.toString
      )

  "EditAuthorityEndDate Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).configure("features.edit-journey" -> true).build()

      running(application) {

        val result = route(application, getRequest).value

        val view = application.injector.instanceOf[EditAuthorityEndDateView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, "someId", "someId")(getRequest, messages(application)).toString
      }
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(EditAuthorityEndDatePage("someId", "someId"), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).configure("features.edit-journey" -> true).build()

      running(application) {

        val view = application.injector.instanceOf[EditAuthorityEndDateView]

        val result = route(application, getRequest).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(validAnswer), "someId", "someId")(getRequest, messages(application)).toString
      }
    }

    "redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .configure("features.edit-journey" -> true).build()

      running(application) {

        val result = route(application, postRequest).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).configure("features.edit-journey" -> true).build()

      running(application) {

        val request =
          fakeRequest(POST, authorityEndDateRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[EditAuthorityEndDateView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, "someId", "someId")(request, messages(application)).toString
      }
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).configure("features.edit-journey" -> true).build()

      running(application) {

        val result = route(application, getRequest).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
      }
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).configure("features.edit-journey" -> true).build()

      running(application) {

        val result = route(application, postRequest).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
      }
    }
  }
}
