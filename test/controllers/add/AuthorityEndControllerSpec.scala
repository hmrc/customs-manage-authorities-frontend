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

package controllers.add

import base.SpecBase
import config.FrontendAppConfig
import forms.AuthorityEndFormProvider
import models.{AuthorityEnd, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.AuthorityEndPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.add.AuthorityEndView

import scala.concurrent.Future

class AuthorityEndControllerSpec extends SpecBase with MockitoSugar {

  private def onwardRoute = Call("GET", "/foo")

  private lazy val authorityEndRoute = controllers.add.routes.AuthorityEndController.onPageLoad(NormalMode).url

  private val formProvider = new AuthorityEndFormProvider()
  private val form = formProvider()
  val backLinkRoute: Call = controllers.add.routes.AuthorityStartController.onPageLoad(NormalMode)

  "AuthorityEnd Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(
        bind[Navigator].toInstance(new FakeNavigator(backLinkRoute))).build()

      running(application) {

        val request = fakeRequest(GET, authorityEndRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AuthorityEndView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, NormalMode,backLinkRoute)(request, messages(application), appConfig).toString
      }
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(AuthorityEndPage, AuthorityEnd.values.head)(AuthorityEnd.writes).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
        bind[Navigator].toInstance(new FakeNavigator(backLinkRoute))).build()

      running(application) {

        val request = fakeRequest(GET, authorityEndRoute)

        val view = application.injector.instanceOf[AuthorityEndView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(AuthorityEnd.values.head), NormalMode,backLinkRoute)(request, messages(application), appConfig).toString
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
          fakeRequest(POST, authorityEndRoute)
            .withFormUrlEncodedBody(("value", AuthorityEnd.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(
        bind[Navigator].toInstance(new FakeNavigator(backLinkRoute))).build()

      running(application) {

        val request =
          fakeRequest(POST, authorityEndRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AuthorityEndView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, NormalMode,backLinkRoute)(request, messages(application), appConfig).toString
      }
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {

        val request = fakeRequest(GET, authorityEndRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
      }
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {

        val request =
          fakeRequest(POST, authorityEndRoute)
            .withFormUrlEncodedBody(("value", AuthorityEnd.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
      }
    }
  }
}
