/*
 * Copyright 2023 HM Revenue & Customs
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
import connectors.{CustomsDataStoreConnector, CustomsFinancialsConnector}
import forms.EoriNumberFormProvider
import models.{CheckMode, CompanyDetails, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.EoriNumberPage
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.StringUtils.emptyString
import views.html.add.EoriNumberView

import scala.concurrent.Future

class EoriNumberControllerSpec extends SpecBase with MockitoSugar {

  "EoriNumber Controller" must {

    "return OK and the correct view for a GET" in new SetUp {

      val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = fakeRequest(GET, eoriNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[EoriNumberView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val xiEoriEnabled = true

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, NormalMode, backLinkRoute, xiEoriEnabled)(request, messages(application), appConfig).toString
      }
    }

    "populate the view correctly on a GET when the question has previously been answered" in new SetUp {

      val userAnswers: UserAnswers =
        UserAnswers(userAnswersId).set(EoriNumberPage, CompanyDetails("answer", Some("1"))).success.value

      val application: Application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = fakeRequest(GET, eoriNumberRoute)

        val view = application.injector.instanceOf[EoriNumberView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        val xiEoriEnabled = true

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill("answer"),
            NormalMode,
            backLinkRoute,
            xiEoriEnabled)(request, messages(application), appConfig).toString
      }
    }

    "redirect to the next page when valid data is submitted" in new SetUp {
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

    "redirect to the next page when valid eori with lowercase is submitted" in new SetUp {
      when(mockConnector.validateEori(any())(any())).thenReturn(Future.successful(Right(true)))

      val mockSessionRepository: SessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application: Application =
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
            .withFormUrlEncodedBody(("value", "gb123456789011"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "redirect to the next page when GBN EORI is submitted" in new SetUp {
      when(mockConnector.validateEori(any())(any())).thenReturn(Future.successful(Right(true)))

      val mockSessionRepository: SessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application: Application =
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
            .withFormUrlEncodedBody(("value", "GBN23456789011"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "redirect to the next page when valid eori with whitespace is submitted" in new SetUp {
      when(mockConnector.validateEori(any())(any())).thenReturn(Future.successful(Right(true)))

      val mockSessionRepository: SessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application: Application =
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

    "redirect to the next page when valid eori with whitespace and lowercase is submitted" in new SetUp {
      when(mockConnector.validateEori(any())(any())).thenReturn(Future.successful(Right(true)))

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application: Application =
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
            .withFormUrlEncodedBody(("value", "gb 12 34 56 78 90 11"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "redirect to EoriDetailsCorrect page when form data is valid and user in NormalMode" in new SetUp {
      when(mockConnector.validateEori(any())(any())).thenReturn(Future.successful(Right(true)))

      private val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application: Application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(
              new FakeNavigator(controllers.add.routes.EoriDetailsCorrectController.onPageLoad(NormalMode))
            ),
            bind[CustomsFinancialsConnector].toInstance(mockConnector),
            bind[SessionRepository].toInstance(mockSessionRepository)
          ).build()

      running(application) {

        val request =
          fakeRequest(POST, eoriNumberNormalModeSubmitRoute)
            .withFormUrlEncodedBody(("value", "gb123456789011"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.add.routes.EoriDetailsCorrectController.onPageLoad(NormalMode).url
      }
    }

    "redirect to EoriDetailsCorrect page when form data is valid, request eori is same as UserAnswers eori" +
      "and user in CheckMode" in new SetUp {
      when(mockConnector.validateEori(any())(any())).thenReturn(Future.successful(Right(true)))

      private val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application: Application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers.set(
          EoriNumberPage, CompanyDetails("gb123456789011", None)).success.value))
          .overrides(
            bind[Navigator].toInstance(
              new FakeNavigator(controllers.add.routes.EoriDetailsCorrectController.onPageLoad(NormalMode))
            ),
            bind[CustomsFinancialsConnector].toInstance(mockConnector),
            bind[SessionRepository].toInstance(mockSessionRepository)
          ).build()

      running(application) {

        val request =
          fakeRequest(POST, eoriNumberCheckModeSubmitRoute)
            .withFormUrlEncodedBody(("value", "gb123456789011"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.add.routes.EoriDetailsCorrectController.onPageLoad(NormalMode).url
      }
    }

    "redirect to EoriDetailsCorrect page when form data is valid, request eori is different to UserAnswers eori" +
      "and user in CheckMode" in new SetUp {
      when(mockConnector.validateEori(any())(any())).thenReturn(Future.successful(Right(true)))

      val mockSessionRepository: SessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application: Application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers.set(
          EoriNumberPage, CompanyDetails("gb123456789011", None)).success.value),
          requestEoriNUmber = "gb123456789033")
          .overrides(
            bind[Navigator].toInstance(
              new FakeNavigator(controllers.add.routes.EoriDetailsCorrectController.onPageLoad(CheckMode))
            ),
            bind[CustomsFinancialsConnector].toInstance(mockConnector),
            bind[SessionRepository].toInstance(mockSessionRepository)
          ).build()

      running(application) {

        val request =
          fakeRequest(POST, eoriNumberCheckModeSubmitRoute)
            .withFormUrlEncodedBody(("value", "gb123456789012"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.add.routes.EoriDetailsCorrectController.onPageLoad(CheckMode).url
      }
    }

    "redirect to EoriDetailsCorrect page when form data is valid, a XI EORI but trader is not registered for " +
      "his own XI EORI" in new SetUp {
      when(mockConnector.validateEori(any())(any())).thenReturn(Future.successful(Right(true)))
      val mockSessionRepository: SessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockDataStoreConnector.getXiEori(any[String])(any[HeaderCarrier])).thenReturn(Future.successful(None))
      when(mockDataStoreConnector.getCompanyName(any[String])(any[HeaderCarrier])).thenReturn(Future.successful(None))

      val application: Application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(
              new FakeNavigator(controllers.add.routes.EoriDetailsCorrectController.onPageLoad(NormalMode))
            ),
            bind[CustomsFinancialsConnector].toInstance(mockConnector),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector)
          ).build()

      running(application) {

        val request =
          fakeRequest(POST, eoriNumberNormalModeSubmitRoute)
            .withFormUrlEncodedBody(("value", "XI123456789012"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.add.routes.EoriDetailsCorrectController.onPageLoad(NormalMode).url
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in new SetUp {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = fakeRequest(POST, eoriNumberRoute).withFormUrlEncodedBody(("value", emptyString))

        val boundForm = form.bind(Map("value" -> emptyString))

        val view = application.injector.instanceOf[EoriNumberView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        val xiEoriEnabled = true

        contentAsString(result) mustEqual
          view(boundForm, NormalMode, backLinkRoute, xiEoriEnabled)(request, messages(application), appConfig).toString
      }
    }

    "return a Bad Request and errors when eori is same as the authorise eori is submitted" in new SetUp {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request =
          fakeRequest(POST, eoriNumberRoute
          )
            .withFormUrlEncodedBody(("value", "GB123456789012"))

        val boundForm =
          form.bind(Map("value" -> "GB123456789012"))
            .withError("value", "eoriNumber.error.authorise-own-eori")

        val view = application.injector.instanceOf[EoriNumberView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        val xiEoriEnabled = true
        contentAsString(result) mustEqual
          view(boundForm, NormalMode, backLinkRoute, xiEoriEnabled)(request, messages(application), appConfig).toString
      }
    }

    "return a Bad Request and errors when invalid EORI is submitted" in new SetUp {

      when(mockConnector.validateEori(any())(any())).thenReturn(Future.successful(Right(false)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(
        bind[CustomsFinancialsConnector].toInstance(mockConnector)
      ).build()

      running(application) {
        val request = fakeRequest(POST, eoriNumberRoute).withFormUrlEncodedBody(("value", "GB123456789011"))

        val boundForm =
          form.bind(Map("value" -> "GB123456789011"))
            .withError("value", "eoriNumber.error.invalid")

        val view = application.injector.instanceOf[EoriNumberView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        val xiEoriEnabled = true

        contentAsString(result) mustEqual
          view(boundForm, NormalMode, backLinkRoute, xiEoriEnabled)(request, messages(application), appConfig).toString
      }
    }

    "return a Bad Request and correct error msg when form data is valid and user " +
      "provides his own XI EORI" in new SetUp {
      val mockSessionRepository: SessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockDataStoreConnector.getXiEori(any[String])(any[HeaderCarrier])).thenReturn(
        Future.successful(Some("XI123456789012")))

      val application: Application =
        applicationBuilder(Some(emptyUserAnswers), "GB123456789011")
          .overrides(
            bind[Navigator].toInstance(
              new FakeNavigator(controllers.add.routes.EoriDetailsCorrectController.onPageLoad(NormalMode))
            ),
            bind[CustomsFinancialsConnector].toInstance(mockConnector),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector)
          ).build()

      running(application) {

        val request =
          fakeRequest(POST, eoriNumberNormalModeSubmitRoute).withFormUrlEncodedBody(("value", "XI123456789012"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result).contains(messages(application)("eoriNumber.error.authorise-own-eori"))
      }
    }
  }

  trait SetUp {
    def onwardRoute: Call = Call("GET", "/foo")

    val formProvider = new EoriNumberFormProvider()
    val form: Form[String] = formProvider()

    val mockConnector: CustomsFinancialsConnector = mock[CustomsFinancialsConnector]
    val mockDataStoreConnector: CustomsDataStoreConnector = mock[CustomsDataStoreConnector]

    implicit lazy val hc: HeaderCarrier = HeaderCarrier()

    lazy val eoriNumberRoute: String = controllers.add.routes.EoriNumberController.onPageLoad(NormalMode).url
    lazy val eoriNumberNormalModeSubmitRoute: String =
      controllers.add.routes.EoriNumberController.onSubmit(NormalMode).url

    lazy val eoriNumberCheckModeSubmitRoute: String =
      controllers.add.routes.EoriNumberController.onSubmit(CheckMode).url

    val backLinkRoute: Call = controllers.routes.ManageAuthoritiesController.onPageLoad
  }
}
