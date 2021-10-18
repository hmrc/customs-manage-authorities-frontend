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
import connectors.CustomsFinancialsConnector
import controllers.actions.{FakeVerifyAccountNumbersAction, VerifyAccountNumbersAction}
import forms.AuthorisedUserFormProviderWithConsent
import models.UserAnswers
import models.domain.{AccountStatusOpen, AccountWithAuthorities, AuthorisedUser, CdsCashAccount, StandingAuthority}
import models.requests.Accounts
import navigation.{FakeNavigator, Navigator}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.AuthorisedUserPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import repositories.SessionRepository
import services.DateTimeService
import services.add.CheckYourAnswersValidationService
import viewmodels.CheckYourAnswersHelper
import views.html.add.AuthorisedUserView

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Future

class AuthorisedUserControllerSpec extends SpecBase with MockitoSugar {

  private def onwardRoute = Call("GET", "/foo")

  private val formProvider = new AuthorisedUserFormProviderWithConsent()
  private val form = formProvider()

  private lazy val authorisedUserRoute = controllers.add.routes.AuthorisedUserController.onPageLoad().url

  val mockConnector: CustomsFinancialsConnector = mock[CustomsFinancialsConnector]
  when(mockConnector.grantAccountAuthorities(any())(any())).thenReturn(Future.successful(true))

  val accounts: Accounts = Accounts(Some(AccountWithAuthorities(CdsCashAccount, "12345", Some(AccountStatusOpen), Seq.empty)), Seq.empty, None)
  val standingAuthority: StandingAuthority = StandingAuthority("GB123456789012", LocalDate.now(), None, viewBalance = true)
  val mockValidator: CheckYourAnswersValidationService = mock[CheckYourAnswersValidationService]
  when(mockValidator.validate(any())).thenReturn(Some((accounts, standingAuthority)))

  val mockDateTimeService: DateTimeService = mock[DateTimeService]
  when(mockDateTimeService.localTime()).thenReturn(LocalDateTime.now())

  "AuthorisedUser Controller" must {

    "return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers

      val application = applicationBuilder(Some(userAnswers))
        .overrides(
          bind[CustomsFinancialsConnector].toInstance(mockConnector),
          bind[CheckYourAnswersValidationService].toInstance(mockValidator),
          bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers))
        )
        .build()

      running(application) {

        val request = fakeRequest(GET, authorisedUserRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AuthorisedUserView]

        val helper = CheckYourAnswersHelper(userAnswers, mockDateTimeService)(messages(application))

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, helper)(request, messages(application)).toString
      }
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val answer = AuthorisedUser("name", "role")

      val userAnswers = UserAnswers(userAnswersId).set(AuthorisedUserPage, answer).success.value

      val application = applicationBuilder(Some(userAnswers))
        .overrides(
          bind[CustomsFinancialsConnector].toInstance(mockConnector),
          bind[CheckYourAnswersValidationService].toInstance(mockValidator),
          bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers))
        )
        .build()

      running(application) {

        val request = fakeRequest(GET, authorisedUserRoute)

        val view = application.injector.instanceOf[AuthorisedUserView]

        val result = route(application, request).value

        val helper = CheckYourAnswersHelper(userAnswers, mockDateTimeService)(messages(application))

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(answer), helper)(request, messages(application)).toString
      }
    }

    "redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CustomsFinancialsConnector].toInstance(mockConnector),
            bind[CheckYourAnswersValidationService].toInstance(mockValidator),
            bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(emptyUserAnswers))
          )
          .build()

      running(application) {

        val request =
          fakeRequest(POST, authorisedUserRoute)
            .withFormUrlEncodedBody(("fullName", "name"), ("jobRole", "role"), ("confirmation", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers

      val application = applicationBuilder(Some(userAnswers))
        .overrides(
          bind[CustomsFinancialsConnector].toInstance(mockConnector),
          bind[CheckYourAnswersValidationService].toInstance(mockValidator),
          bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers))
        )
        .build()

      running(application) {

        val request =
          fakeRequest(POST, authorisedUserRoute
        )
        .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AuthorisedUserView]

        val result = route(application, request).value

        val helper = CheckYourAnswersHelper(userAnswers, mockDateTimeService)(messages(application))

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, helper)(request, messages(application)).toString
      }
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {

        val request = fakeRequest(GET, authorisedUserRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
      }
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {

        val request =
          fakeRequest(POST, authorisedUserRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
      }
    }

    "return a Bad Request when name contains malicious code" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[CustomsFinancialsConnector].toInstance(mockConnector),
            bind[CheckYourAnswersValidationService].toInstance(mockValidator),
            bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(emptyUserAnswers))
          )
          .build()

      running(application) {

        val request =
          fakeRequest(POST, authorisedUserRoute)
            .withFormUrlEncodedBody(("fullName", "<script>"), ("jobRole", "role"), ("confirmation", "true"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "return a Bad Request when job role contains malicious code" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[CustomsFinancialsConnector].toInstance(mockConnector),
            bind[CheckYourAnswersValidationService].toInstance(mockValidator),
            bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(emptyUserAnswers))
          )
          .build()

      running(application) {

        val request =
          fakeRequest(POST, authorisedUserRoute)
            .withFormUrlEncodedBody(("fullName", "name"), ("jobRole", "alert(1)"), ("confirmation", "true"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }
  }
}
