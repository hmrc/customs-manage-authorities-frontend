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
import config.FrontendAppConfig
import forms.AuthorityStartFormProvider
import models.{AuthorityStart, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.edit.EditAuthorityStartPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.edit.EditAuthorityStartView

import scala.concurrent.Future

class EditAuthorityStartControllerSpec extends SpecBase with MockitoSugar {

  private def onwardRoute = Call("GET", "/foo")

  private lazy val authorityStartRoute = controllers.edit.routes.EditAuthorityStartController.onPageLoad("someId", "someId").url

  private val formProvider = new AuthorityStartFormProvider()
  private val form = formProvider()

  "EditAuthorityStart Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).configure("features.edit-journey" -> true).build()

      running(application) {

        val request = fakeRequest(GET, authorityStartRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[EditAuthorityStartView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, "someId", "someId")(request, messages(application), appConfig).toString
      }
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(EditAuthorityStartPage("someId", "someId"), AuthorityStart.values.head)(AuthorityStart.writes).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).configure("features.edit-journey" -> true).build()

      running(application) {

        val request = fakeRequest(GET, authorityStartRoute)

        val view = application.injector.instanceOf[EditAuthorityStartView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(AuthorityStart.values.head), "someId", "someId")(request, messages(application), appConfig).toString
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

        val request =
          fakeRequest(POST, authorityStartRoute)
            .withFormUrlEncodedBody(("value", AuthorityStart.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).configure("features.edit-journey" -> true).build()

      running(application) {

        val request =
          fakeRequest(POST, authorityStartRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[EditAuthorityStartView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, "someId", "someId")(request, messages(application), appConfig).toString
      }
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).configure("features.edit-journey" -> true).build()

      running(application) {

        val request = fakeRequest(GET, authorityStartRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
      }
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).configure("features.edit-journey" -> true).build()

      running(application) {

        val request =
          fakeRequest(POST, authorityStartRoute)
            .withFormUrlEncodedBody(("value", AuthorityStart.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
      }
    }
  }
}
