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
import forms.AuthorityStartFormProvider
import models.{AuthorityStart, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.AuthorityStartPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.add.AuthorityStartView

import scala.concurrent.Future

class AuthorityStartControllerSpec extends SpecBase with MockitoSugar {

  private lazy val authorityStartRoute = controllers.add.routes.AuthorityStartController.onPageLoad(NormalMode).url

  private def onwardRoute  = Call("GET", "/foo")
  private val formProvider = new AuthorityStartFormProvider()
  private val form         = formProvider()
  val backLinkRoute: Call  = controllers.add.routes.AccountsController.onPageLoad(NormalMode)

  "AuthorityStart Controller" must {
    "return OK and the correct view for a GET in Normal mode" in {

      running(application(Some(emptyUserAnswers))) {

        val request = fakeRequest(GET, authorityStartRoute)
        val result  = route(application(Some(emptyUserAnswers)), request).value
        val view    = application(Some(emptyUserAnswers)).injector.instanceOf[AuthorityStartView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, NormalMode, backLinkRoute)(request, messages, appConfig).toString
      }
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId.value)
        .set(AuthorityStartPage, AuthorityStart.values.head)(AuthorityStart.writes)
        .success
        .value

      running(application(Some(userAnswers))) {

        val request = fakeRequest(GET, authorityStartRoute)
        val view    = application(Some(userAnswers)).injector.instanceOf[AuthorityStartView]
        val result  = route(application(Some(userAnswers)), request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(AuthorityStart.values.head), NormalMode, backLinkRoute)(
            request,
            messages,
            appConfig
          ).toString
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
          fakeRequest(POST, authorityStartRoute)
            .withFormUrlEncodedBody(("value", AuthorityStart.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      running(application(Some(emptyUserAnswers))) {
        val request =
          fakeRequest(POST, authorityStartRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))
        val view      = application(Some(emptyUserAnswers)).injector.instanceOf[AuthorityStartView]
        val result    = route(application(Some(emptyUserAnswers)), request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, NormalMode, backLinkRoute)(request, messages, appConfig).toString
      }
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      running(application(None)) {
        val request = fakeRequest(GET, authorityStartRoute)
        val result  = route(application(None), request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
      }
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      running(application(None)) {
        val request =
          fakeRequest(POST, authorityStartRoute)
            .withFormUrlEncodedBody(("value", AuthorityStart.values.head.toString))

        val result = route(application(None), request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
      }
    }
  }
}
