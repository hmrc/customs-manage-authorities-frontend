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
import connectors.CustomsFinancialsConnector
import controllers.actions.{FakeVerifyAccountNumbersAction, VerifyAccountNumbersAction}
import forms.AuthorisedUserFormProvider
import models.domain.{AccountStatusOpen, AccountWithAuthorities, AccountWithAuthoritiesWithId, AuthorisedUser, AuthoritiesWithId, CdsCashAccount, StandingAuthority}
import models.requests.{Accounts, AddAuthorityRequest}
import models.{AuthorityEnd, AuthorityStart, ShowBalance, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.edit.{EditAuthorityEndPage, EditAuthorityStartPage, EditShowBalancePage}
import play.api.Application
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import repositories.{AuthoritiesRepository, SessionRepository}
import services.DateTimeService
import services.add.CheckYourAnswersValidationService
import services.edit.EditAuthorityValidationService
import viewmodels.CheckYourAnswersEditHelper
import views.html.edit.EditAuthorisedUserView

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Future

class EditCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {
  "EditCheckYourAnswers Controller" must {

    "return OK and the correct view for a GET" in new Setup {

      val userAnswers = emptyUserAnswers

      val application = applicationBuilder(Some(userAnswers))
        .overrides(
          bind[CustomsFinancialsConnector].toInstance(mockConnector),
          bind[CheckYourAnswersValidationService].toInstance(mockValidator),
          bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers)),
          bind[AuthoritiesRepository].toInstance(mockAuthoritiesRepo)
        ).configure(Map("features.edit-journey" -> true))
        .build()

      when(mockAuthoritiesRepo.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))

      running(application) {

        val request = fakeRequest(GET, authorisedUserRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[EditAuthorisedUserView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, helper(userAnswers, application), "a", "b")(request, messages(application)).toString
      }
    }

    "populate the view correctly on a GET when the question has previously been answered" in new Setup {

      val userAnswers = populatedUserAnswers(emptyUserAnswers)

      val newStandingAuthority = standingAuthority.copy(authorisedFromDate = LocalDate.now().plusDays(20))

      val newAccountsWithAuthoritiesWithId = AccountWithAuthoritiesWithId(CdsCashAccount, "12345", Some(AccountStatusOpen), Map("b" -> newStandingAuthority))
      val newAuthoritiesWithId: AuthoritiesWithId = AuthoritiesWithId(Map(
        ("a" -> newAccountsWithAuthoritiesWithId)
      ))

      val application = applicationBuilder(Some(userAnswers))
        .overrides(
          bind[CustomsFinancialsConnector].toInstance(mockConnector),
          bind[CheckYourAnswersValidationService].toInstance(mockValidator),
          bind[AuthoritiesRepository].toInstance(mockAuthoritiesRepo),
          bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers))
        ).configure(Map("features.edit-journey" -> true))
        .build()

      when(mockAuthoritiesRepo.get(any())).thenReturn(Future.successful(Some(newAuthoritiesWithId)))

      running(application) {

        val request = fakeRequest(GET, authorisedUserRoute)

        val view = application.injector.instanceOf[EditAuthorisedUserView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, helper(userAnswers, application, newStandingAuthority), "a", "b")(request, messages(application)).toString
      }
    }

    "redirect to the next page when valid data is submitted" in new Setup {

      val mockSessionRepository = mock[SessionRepository]
      val mockEditAuthorityValidationService = mock[EditAuthorityValidationService]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = populatedUserAnswers(emptyUserAnswers)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CustomsFinancialsConnector].toInstance(mockConnector),
            bind[EditAuthorityValidationService].toInstance(mockEditAuthorityValidationService),
            bind[CheckYourAnswersValidationService].toInstance(mockValidator),
            bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers)),
            bind[AuthoritiesRepository].toInstance(mockAuthoritiesRepo)
          ).configure(Map("features.edit-journey" -> true))
          .build()

      when(mockAuthoritiesRepo.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))
      when(mockConnector.grantAccountAuthorities(any())(any())).thenReturn(Future.successful(true))
      val accounts = Accounts(Some(AccountWithAuthorities(CdsCashAccount, "12345", Some(AccountStatusOpen), Seq.empty)), Seq.empty, None)
      when(mockEditAuthorityValidationService.validate(any(), any(), any(), any(), any()))
        .thenReturn(Right(AddAuthorityRequest(accounts, standingAuthority, AuthorisedUser("someName", "someRole"),true)))

      running(application) {

        val request =
          fakeRequest(POST, authorisedUserRoute)
            .withFormUrlEncodedBody(("fullName", "name"), ("jobRole", "role"), ("confirmation", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "redirect to the check your answers page when submitting an edited start date when the date has already been passed" in new Setup {
      val mockSessionRepository = mock[SessionRepository]
      val mockEditAuthorityValidationService = mock[EditAuthorityValidationService]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = populatedUserAnswers(emptyUserAnswers)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CustomsFinancialsConnector].toInstance(mockConnector),
            bind[EditAuthorityValidationService].toInstance(mockEditAuthorityValidationService),
            bind[CheckYourAnswersValidationService].toInstance(mockValidator),
            bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers)),
            bind[AuthoritiesRepository].toInstance(mockAuthoritiesRepo)
          ).configure(Map("features.edit-journey" -> true))
          .build()

      when(mockAuthoritiesRepo.get(any())).thenReturn(Future.successful(Some(authoritiesWithIdPast)))
      when(mockConnector.grantAccountAuthorities(any())(any())).thenReturn(Future.successful(true))
      val accounts = Accounts(Some(AccountWithAuthorities(CdsCashAccount, "12345", Some(AccountStatusOpen), Seq.empty)), Seq.empty, None)
      when(mockEditAuthorityValidationService.validate(any(), any(), any(), any(), any()))
        .thenReturn(Right(AddAuthorityRequest(accounts, standingAuthorityPast, AuthorisedUser("someName", "someRole"),true)))

      running(application) {

        val request =
          fakeRequest(POST, authorisedUserRoute)
            .withFormUrlEncodedBody(("fullName", "name"), ("jobRole", "role"), ("confirmation", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.edit.routes.EditCheckYourAnswersController.onPageLoad("a", "b").url
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in new Setup {

      val userAnswers = emptyUserAnswers
      val application = applicationBuilder(Some(populatedUserAnswers(userAnswers)))
        .overrides(
          bind[CustomsFinancialsConnector].toInstance(mockConnector),
          bind[CheckYourAnswersValidationService].toInstance(mockValidator),
          bind[AuthoritiesRepository].toInstance(mockAuthoritiesRepo),
          bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers))
        ).configure(Map("features.edit-journey" -> true))
        .build()

      when(mockAuthoritiesRepo.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))

      running(application) {

        val request =
          fakeRequest(POST, authorisedUserRoute
          )
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[EditAuthorisedUserView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, helper(populatedUserAnswers(userAnswers), application), "a", "b")(request, messages(application)).toString
      }
    }

    "redirect to Session Expired for a POST if no existing data is found" in new Setup {

      val application = applicationBuilder(userAnswers = None).configure(Map("features.edit-journey" -> true)).build()

      running(application) {

        val request =
          fakeRequest(POST, authorisedUserRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
      }
    }
  }

  trait Setup {
    def onwardRoute = Call("GET", "/foo")

    val formProvider = new AuthorisedUserFormProvider()
    val form = formProvider()

    lazy val authorisedUserRoute = controllers.edit.routes.EditCheckYourAnswersController.onPageLoad("a", "b").url

    val mockConnector = mock[CustomsFinancialsConnector]
    when(mockConnector.grantAccountAuthorities(any())(any())).thenReturn(Future.successful(true))

    def populatedUserAnswers(userAnswers: UserAnswers) = {
      userAnswers.set(EditShowBalancePage("a", "b"), ShowBalance.Yes)(ShowBalance.writes).success.value
        .set(EditAuthorityStartPage("a", "b"), AuthorityStart.Today)(AuthorityStart.writes).success.value
        .set(EditAuthorityEndPage("a", "b"), AuthorityEnd.Indefinite)(AuthorityEnd.writes).success.value
    }

    val standingAuthority = StandingAuthority("GB123456789012", LocalDate.now(), None, viewBalance = true)

    val accountsWithAuthoritiesWithId = AccountWithAuthoritiesWithId(CdsCashAccount, "12345", Some(AccountStatusOpen), Map("b" -> standingAuthority))
    val authoritiesWithId: AuthoritiesWithId = AuthoritiesWithId(Map(
      ("a" -> accountsWithAuthoritiesWithId)
    ))

    val standingAuthorityPast = StandingAuthority("GB123456789012", LocalDate.now().minusDays(2), None, viewBalance = true)

    val accountsWithAuthoritiesWithIdPast = AccountWithAuthoritiesWithId(CdsCashAccount, "12345", Some(AccountStatusOpen), Map("b" -> standingAuthorityPast))
    val authoritiesWithIdPast: AuthoritiesWithId = AuthoritiesWithId(Map(
      ("a" -> accountsWithAuthoritiesWithIdPast)
    ))

    def helper(userAnswers: UserAnswers, application: Application, authority: StandingAuthority = standingAuthority) = new CheckYourAnswersEditHelper(
      populatedUserAnswers(userAnswers),
      "a",
      "b",
      mockDateTimeService,
      authority,
      accountsWithAuthoritiesWithId)(messages(application))

    val mockValidator = mock[CheckYourAnswersValidationService]
    val mockAuthoritiesRepo = mock[AuthoritiesRepository]
    val mockDateTimeService = mock[DateTimeService]
    when(mockDateTimeService.localTime()).thenReturn(LocalDateTime.now())
    when(mockDateTimeService.localDate()).thenReturn(LocalDate.now())
  }

}
