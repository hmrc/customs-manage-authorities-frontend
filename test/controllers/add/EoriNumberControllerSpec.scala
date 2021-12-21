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
import connectors.CustomsFinancialsConnector
import forms.EoriNumberFormProvider
import models.{CompanyDetails, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.EoriNumberPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.add.EoriNumberView

import scala.concurrent.Future

class EoriNumberControllerSpec extends SpecBase with MockitoSugar {

  private def onwardRoute = Call("GET", "/foo")

  private val formProvider = new EoriNumberFormProvider()
  private val form = formProvider()
  val mockConnector = mock[CustomsFinancialsConnector]

  private lazy val eoriNumberRoute = controllers.add.routes.EoriNumberController.onPageLoad(NormalMode).url
  val backLinkRoute: Call = controllers.routes.ManageAuthoritiesController.onPageLoad
  private lazy val GBNValidationRoute = controllers.add.routes.GBNEoriController.showGBNEori().url

  "EoriNumber Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = fakeRequest(GET, eoriNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[EoriNumberView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]


        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, NormalMode,backLinkRoute)(request, messages(application), appConfig).toString
      }
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(EoriNumberPage, CompanyDetails("answer", Some("1"))).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = fakeRequest(GET, eoriNumberRoute)

        val view = application.injector.instanceOf[EoriNumberView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill("answer"), NormalMode,backLinkRoute)(request, messages(application), appConfig).toString
      }
    }

    "redirect to the next page when valid data is submitted" in {
      when(mockConnector.validateEori(any())(any())).thenReturn(Future.successful(Right(true)))

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[CustomsFinancialsConnector].toInstance(mockConnector),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          fakeRequest(POST, eoriNumberRoute)
            .withFormUrlEncodedBody(("value", "GB123456789011"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "redirect to the next page when valid eori with whitespace is submitted" in {
      when(mockConnector.validateEori(any())(any())).thenReturn(Future.successful(Right(true)))

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[CustomsFinancialsConnector].toInstance(mockConnector),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          fakeRequest(POST, eoriNumberRoute)
            .withFormUrlEncodedBody(("value", "GB 12 34 56 78 90 11"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request =
          fakeRequest(POST, eoriNumberRoute
        )
        .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[EoriNumberView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, NormalMode,backLinkRoute)(request, messages(application), appConfig).toString
      }
    }

    "return a Bad Request and errors when eori is same as the authorise eori is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request =
          fakeRequest(POST, eoriNumberRoute
          )
            .withFormUrlEncodedBody(("value", "GB123456789012"))

        val boundForm = form.bind(Map("value" -> "GB123456789012")).withError("value", "eoriNumber.error.authorise-own-eori")

        val view = application.injector.instanceOf[EoriNumberView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, NormalMode,backLinkRoute)(request, messages(application), appConfig).toString
      }
    }

    "return a Bad Request and errors when invalid EORI is submitted" in {

      when(mockConnector.validateEori(any())(any())).thenReturn(Future.successful(Right(false)))
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(
        bind[CustomsFinancialsConnector].toInstance(mockConnector),
      ).build()
      running(application) {
        val request =
          fakeRequest(POST, eoriNumberRoute
          )
            .withFormUrlEncodedBody(("value", "GB123456789011"))

        val boundForm = form.bind(Map("value" -> "GB123456789011")).withError("value", "eoriNumber.error.invalid")

        val view = application.injector.instanceOf[EoriNumberView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, NormalMode,backLinkRoute)(request, messages(application), appConfig).toString
      }
    }


    "redirect to GBNEoriErrorView page when GBN EORI is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(
        bind[CustomsFinancialsConnector].toInstance(mockConnector),
      ).build()
      running(application) {
        val request =
          fakeRequest(POST, eoriNumberRoute
          )
            .withFormUrlEncodedBody(("value", "GBN123456789011"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual GBNValidationRoute

      }
    }

  }
}
