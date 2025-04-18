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
import forms.AuthorityDetailsFormProvider
import models.domain.AuthorisedUser
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.AuthorityDetailsPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import repositories.SessionRepository
import utils.StringUtils.emptyString
import views.html.add.AuthorityDetailsView

import scala.concurrent.Future

class AuthorityDetailsControllerSpec extends SpecBase with MockitoSugar {

  private def onwardRoute = Call("GET", "/foo")

  private lazy val authorityDetailsRoute = controllers.add.routes.AuthorityDetailsController.onPageLoad(NormalMode).url
  private val formProvider               = new AuthorityDetailsFormProvider()
  private val form                       = formProvider()
  val backLinkRoute: Call                = controllers.add.routes.ShowBalanceController.onPageLoad(NormalMode)

  "AuthorityDetailsCorrect Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = fakeRequest(GET, authorityDetailsRoute)

        val result = route(application, request).value

        val view      = application.injector.instanceOf[AuthorityDetailsView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, NormalMode, backLinkRoute)(request, messages(application), appConfig).toString
      }
    }
    "populate the view correctly on a GET when the question has previously been answered" in {

      val answer = AuthorisedUser("name", "role")

      val userAnswers = UserAnswers(userAnswersId.value).set(AuthorityDetailsPage, answer).success.value

      val application = applicationBuilder(Some(userAnswers))
        .overrides()
        .build()

      running(application) {

        val request = fakeRequest(GET, authorityDetailsRoute)

        val view      = application.injector.instanceOf[AuthorityDetailsView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(answer), NormalMode, backLinkRoute)(request, messages(application), appConfig).toString
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
          .build()

      running(application) {

        val request =
          fakeRequest(POST, authorityDetailsRoute)
            .withFormUrlEncodedBody(("fullName", "name"), ("jobRole", "role"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers

      val application = applicationBuilder(Some(userAnswers))
        .overrides()
        .build()

      running(application) {

        val request = fakeRequest(POST, authorityDetailsRoute).withFormUrlEncodedBody(("value", emptyString))

        val boundForm = form.bind(Map("value" -> emptyString))

        val view      = application.injector.instanceOf[AuthorityDetailsView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, NormalMode, backLinkRoute)(request, messages(application), appConfig).toString
      }
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {

        val request = fakeRequest(GET, authorityDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
      }
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {

        val request =
          fakeRequest(POST, authorityDetailsRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
      }
    }

    "return a Bad Request when name contains malicious code" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides()
          .build()

      running(application) {

        val request =
          fakeRequest(POST, authorityDetailsRoute)
            .withFormUrlEncodedBody(("fullName", "<script>"), ("jobRole", "role"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "return a Bad Request when job role contains malicious code" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides()
          .build()

      running(application) {

        val request =
          fakeRequest(POST, authorityDetailsRoute)
            .withFormUrlEncodedBody(("fullName", "name"), ("jobRole", "alert(1)"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }
  }
}
