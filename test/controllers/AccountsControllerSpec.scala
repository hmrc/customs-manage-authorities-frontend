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

package controllers

import base.SpecBase
import config.FrontendAppConfig
import forms.AccountsFormProvider
import models.domain.{AccountStatusClosed, AccountStatusOpen, AccountWithAuthorities, AuthoritiesWithId, CDSAccounts, CDSCashBalance, CashAccount, CdsCashAccount, StandingAuthority}
import models.{AuthorisedAccounts, InternalId, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.{AccountsPage, EoriNumberPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{AccountsCacheService, AuthoritiesCacheService}
import uk.gov.hmrc.http.InternalServerException
import views.html.{AccountsView, NoAvailableAccountsView, ServiceUnavailableView}

import java.time.LocalDate
import scala.concurrent.Future

class AccountsControllerSpec extends SpecBase with MockitoSugar {
  "Accounts Controller" must {

    "return OK and the correct view for a GET" when {

      "user answers exists" in new Setup {
        val application = applicationBuilder(userAnswers = Some(userAnswersWithEori))
          .overrides(bind[AccountsCacheService].toInstance(mockAccountsCacheService))
          .overrides(bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService))
          .build()

        running(application) {

          val request = fakeRequest(GET, accountsRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[AccountsView]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form, AuthorisedAccounts(Seq.empty, answerAccounts, Seq(
              CashAccount("23456", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(100.00)))
            ), Seq.empty, "GB9876543210000"), NormalMode,backLinkRoute)(request, messages(application), appConfig).toString
        }
      }

      "user answers exists with no entered EORI" in new Setup {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

        running(application) {

          val request = fakeRequest(GET, accountsRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.add.routes.EoriNumberController.onPageLoad(NormalMode).url
        }
      }

      "user answers doesn't exist" in new Setup {
        val application = applicationBuilder(userAnswers = None)
          .overrides(bind[AccountsCacheService].toInstance(mockAccountsCacheService))
          .build()

        running(application) {

          val request = fakeRequest(GET, accountsRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
        }
      }

      "render service unavailable when the API call fails" in new Setup {

        val failingMockService = mock[AccountsCacheService]

        when(failingMockService.retrieveAccounts(any[InternalId](), any())(any()))
          .thenReturn(Future.failed(new InternalServerException("broken")))

        val application = applicationBuilder(Some(userAnswersWithEori))
          .overrides(bind[AccountsCacheService].toInstance(failingMockService))
          .build()

        running(application) {

          val request = fakeRequest(GET, accountsRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ServiceUnavailableView]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          status(result) mustEqual INTERNAL_SERVER_ERROR

          contentAsString(result) mustEqual
            view()(request, messages(application), appConfig).toString
        }
      }
    }

    "return OK and the no accounts available view for a GET" when {
      "there are no available accounts to select" in new Setup {
        val application = applicationBuilder(userAnswers = Some(userAnswersWithEori))
          .overrides(bind[AccountsCacheService].toInstance(mockAccountsCacheService))
          .overrides(bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService))
          .build()

        val noAvailableAccounts = CDSAccounts("GB123456789012", List(
          CashAccount("12345", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(100.00))),
          CashAccount("23456", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(100.00)))
        ))
        when(mockAccountsCacheService.retrieveAccounts(any[InternalId](), any())(any())).thenReturn(Future.successful(noAvailableAccounts))

        running(application) {

          val request = fakeRequest(GET, accountsRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[NoAvailableAccountsView]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view("GB9876543210000")(request, messages(application), appConfig).toString
        }
      }
    }

    "populate the view correctly on a GET when the question has previously been answered" in new Setup {

      val userAnswers = UserAnswers(userAnswersId)
        .set(AccountsPage, answerAccounts).success.value
        .set(EoriNumberPage, "GB9876543210000").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[AccountsCacheService].toInstance(mockAccountsCacheService))
        .overrides(bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService))
        .build()

      running(application) {

        val request = fakeRequest(GET, accountsRoute)

        val view = application.injector.instanceOf[AccountsView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(answer), AuthorisedAccounts(Seq.empty, answerAccounts, Seq(
            CashAccount("23456", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(100.00)))
          ), Seq.empty, "GB9876543210000"), NormalMode,backLinkRoute)(request, messages(application), appConfig).toString
      }
    }

    "redirect to the next page when valid data is submitted" when {

      "user answers exists" in new Setup {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithEori))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[AccountsCacheService].toInstance(mockAccountsCacheService),
              bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService)
            )
            .build()

        running(application) {

          val request =
            fakeRequest(POST, accountsRoute)
              .withFormUrlEncodedBody(("value[0]", answer.head))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "user answers exists with no EORI" in new Setup {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[AccountsCacheService].toInstance(mockAccountsCacheService),
              bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService)
            )
            .build()

        running(application) {

          val request =
            fakeRequest(POST, accountsRoute)
              .withFormUrlEncodedBody(("value[0]", answer.head))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.add.routes.EoriNumberController.onPageLoad(NormalMode).url
        }
      }

      "user answers doesn't exist" in new Setup {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = None)
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[AccountsCacheService].toInstance(mockAccountsCacheService)
            )
            .build()

        running(application) {

          val request =
            fakeRequest(POST, accountsRoute)
              .withFormUrlEncodedBody(("value[0]", answer.head))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
        }
      }

    }

    "return a Bad Request and errors when invalid data is submitted" in new Setup {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithEori))
        .overrides(
          bind[AccountsCacheService].toInstance(mockAccountsCacheService),
          bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService)
        ).build()

      running(application) {

        val request =
          fakeRequest(POST, accountsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AccountsView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, AuthorisedAccounts(Seq.empty, answerAccounts, Seq(
            CashAccount("23456", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(100.00)))
          ), Seq.empty, "GB9876543210000"), NormalMode,backLinkRoute)(request, messages(application), appConfig).toString
      }
    }
  }
  trait Setup {

    def onwardRoute = Call("GET", "/foo")

    lazy val accountsRoute = controllers.add.routes.AccountsController.onPageLoad(NormalMode).url

    val formProvider = new AccountsFormProvider()
    val form = formProvider()
    val userAnswersWithEori = emptyUserAnswers.copy(data = Json.obj("eoriNumber" -> "GB9876543210000"))

    val standingAuthority = StandingAuthority(
      "EORI",
      LocalDate.parse("2020-03-01"),
      viewBalance = false
    )

    val accountWithAuthorities = AccountWithAuthorities(CdsCashAccount, "54321", Some(AccountStatusOpen), Seq(standingAuthority))

    val answer = List("account_0")
    val answerAccounts = List(CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00))))
    val accounts = CDSAccounts("GB123456789012", List(
      CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00))),
      CashAccount("23456", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(100.00)))
    ))

    val mockAccountsCacheService = mock[AccountsCacheService]
    val mockAuthoritiesCacheService = mock[AuthoritiesCacheService]
    when(mockAccountsCacheService.retrieveAccounts(any[InternalId](), any())(any())).thenReturn(Future.successful(accounts))
    when(mockAuthoritiesCacheService.retrieveAuthorities(any[InternalId])(any())).thenReturn(Future.successful(AuthoritiesWithId(Seq.empty)))
    val backLinkRoute: Call = controllers.add.routes.EoriNumberController.onPageLoad(NormalMode)
  }
}


