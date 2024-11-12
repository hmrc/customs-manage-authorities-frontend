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

package controllers

import base.SpecBase
import config.FrontendAppConfig
import forms.AccountsFormProvider
import models.domain.{
  AccountStatusClosed, AccountStatusOpen, CDSAccounts, CDSCashBalance, CashAccount,
  DutyDefermentAccount, DutyDefermentBalance, StandingAuthority
}
import models.{AuthorisedAccounts, CheckMode, CompanyDetails, InternalId, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.{AccountsPage, EoriNumberPage}
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{AccountsCacheService, AuthorisedAccountsService, AuthoritiesCacheService}
import uk.gov.hmrc.http.InternalServerException
import utils.StringUtils.emptyString
import views.html.{AccountsView, NoAvailableAccountsView, ServiceUnavailableView}

import java.time.LocalDate
import scala.concurrent.Future

class AccountsControllerSpec extends SpecBase with MockitoSugar {
  "Accounts Controller" must {

    "return OK and the correct view for a GET" when {

      "user answers exists with no entered EORI" in new Setup {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = fakeRequest(GET, accountsRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.add.routes.EoriNumberController.onPageLoad(NormalMode).url
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

        val application = applicationBuilder(Some(userAnswersCompanyDetails))
          .overrides(bind[AccountsCacheService].toInstance(failingMockService)).build()

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

      "user answers exists with GB" in new Setup {
        val application = applicationBuilder(userAnswers = Some(userAnswersCompanyDetails))
          .overrides(bind[AccountsCacheService].toInstance(mockAccountsCacheService))
          .overrides(bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService))
          .overrides(bind[AuthorisedAccountsService].toInstance(mockAuthorisedAccountService))
          .build()

        running(application) {

          val request = fakeRequest(GET, accountsRoute)
          val result = route(application, request).value
          val view = application.injector.instanceOf[AccountsView]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form,
              AuthorisedAccounts(
                Seq.empty,
                answerAccounts,
                Seq(CashAccount("23456", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(bigDecimal100)))),
                Seq.empty, "GB9876543210000"),
              NormalMode,
              backLinkRoute)(request, messages(application), appConfig).toString
        }
      }

      "user answers exists with XI" in new Setup {
        val application = applicationBuilder(userAnswers = Some(userAnswersCompanyDetailsXI))
          .overrides(bind[AccountsCacheService].toInstance(mockAccountsCacheService))
          .overrides(bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService))
          .overrides(bind[AuthorisedAccountsService].toInstance(mockAuthorisedAccountService))
          .build()

        when(mockAuthorisedAccountService.getAuthorisedAccounts(any())(any(), any()))
          .thenReturn(Future.successful(AuthorisedAccounts(Seq.empty,
            authorisedAccounts, closedAccountXI, Seq.empty, "XI9876543210000")))

        running(application) {

          val request = fakeRequest(GET, accountsRoute)
          val result = route(application, request).value
          val view = application.injector.instanceOf[AccountsView]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          contentAsString(result) mustEqual view(form,
            AuthorisedAccounts(
              Seq.empty, answerAccounts, Seq(DutyDefermentAccount(
                "123",
                "XI9876543210000",
                AccountStatusOpen,
                DutyDefermentBalance(Some(bigDecimal100), Some(bigDecimal100), Some(bigDecimal100), Some(bigDecimal100)),
                true)),
              Seq.empty, "XI9876543210000"),
            NormalMode,
            backLinkRoute)(request, messages(application), appConfig).toString
        }
      }
    }

    "return OK and the no accounts available view for a GET" when {
      "there are no available accounts to select" in new Setup {
        val application = applicationBuilder(userAnswers = Some(userAnswersCompanyDetails))
          .overrides(bind[AccountsCacheService].toInstance(mockAccountsCacheService))
          .overrides(bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService))
          .overrides(bind[AuthorisedAccountsService].toInstance(mockAuthorisedAccountService))
          .build()

        val noAvailableAccounts = CDSAccounts("GB123456789012", List.empty)

        when(mockAuthorisedAccountService.getAuthorisedAccounts(any())(any(), any()))
          .thenReturn(Future.successful(AuthorisedAccounts(Seq.empty, Seq.empty, Seq.empty, Seq.empty, "GB9876543210000")))
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
        .set(EoriNumberPage, CompanyDetails("GB9876543210000", Some("name"))).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[AccountsCacheService].toInstance(mockAccountsCacheService))
        .overrides(bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService))
        .overrides(bind[AuthorisedAccountsService].toInstance(mockAuthorisedAccountService))
        .build()

      running(application) {

        val request = fakeRequest(GET, accountsRoute)

        val view = application.injector.instanceOf[AccountsView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(answer), AuthorisedAccounts(Seq.empty, answerAccounts, Seq(
            CashAccount("23456", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(bigDecimal100)))
          ), Seq.empty, "GB9876543210000"), NormalMode, backLinkRoute)(request, messages(application), appConfig).toString
      }
    }

    "populate the view correctly on a GET in CheckMode when the question has previously been answered" in new Setup {

      val userAnswers: UserAnswers = UserAnswers(userAnswersId)
        .set(AccountsPage, answerAccounts).success.value
        .set(EoriNumberPage, CompanyDetails("GB9876543210000", Some("name"))).success.value

      val application: Application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[AccountsCacheService].toInstance(mockAccountsCacheService))
        .overrides(bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService))
        .overrides(bind[AuthorisedAccountsService].toInstance(mockAuthorisedAccountService))
        .build()

      running(application) {

        val request = fakeRequest(GET, accountsRouteInCheckMode)
        val view = application.injector.instanceOf[AccountsView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form.fill(answer),
            AuthorisedAccounts(
              Seq.empty,
              answerAccounts,
              Seq(CashAccount("23456", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(bigDecimal100)))),
              Seq.empty, "GB9876543210000"),
            CheckMode,
            backLinkRouteInCheckMode)(request, messages(application), appConfig).toString
      }
    }

    "redirect to the next page when valid data is submitted" when {

      "user answers exists" in new Setup {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(userAnswersCompanyDetails))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[AccountsCacheService].toInstance(mockAccountsCacheService),
              bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService),
              bind[AuthorisedAccountsService].toInstance(mockAuthorisedAccountService)
            )
            .build()

        running(application) {

          val request = fakeRequest(POST, accountsRoute)
            .withFormUrlEncodedBody(("value[0]", answer.head))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "user answers exists in NormalMode" in new Setup {

        val mockSessionRepository: SessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application: Application =
          applicationBuilder(userAnswers = Some(userAnswersCompanyDetails))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(authStartNormalModeRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[AccountsCacheService].toInstance(mockAccountsCacheService),
              bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService),
              bind[AuthorisedAccountsService].toInstance(mockAuthorisedAccountService)
            ).build()

        running(application) {
          val request = fakeRequest(POST, accountsSubmitRouteInNormalMode)
            .withFormUrlEncodedBody(("value[0]", answer.head))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.add.routes.AuthorityStartController.onPageLoad(NormalMode).url
        }
      }

      "Incorrect user answers exists in NormalMode" in new Setup {

        val mockSessionRepository: SessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application: Application =
          applicationBuilder(userAnswers = Some(userAnswersCompanyDetailsXI))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(authStartNormalModeRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[AccountsCacheService].toInstance(mockAccountsCacheService),
              bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService),
              bind[AuthorisedAccountsService].toInstance(mockAuthorisedAccountService)
            ).build()

        running(application) {
          val request = fakeRequest(POST, accountsSubmitRouteInNormalMode)
            .withFormUrlEncodedBody(("value[0]", emptyString))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
        }
      }

      "user answers exists in NormalMode with XI" in new Setup {

        val mockSessionRepository: SessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application: Application =
          applicationBuilder(userAnswers = Some(userAnswersCompanyDetailsXI))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(authStartNormalModeRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[AccountsCacheService].toInstance(mockAccountsCacheService),
              bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService),
              bind[AuthorisedAccountsService].toInstance(mockAuthorisedAccountService)
            ).build()

        running(application) {
          val request = fakeRequest(POST, accountsSubmitRouteInNormalMode)
            .withFormUrlEncodedBody(("value[0]", answer.head))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.add.routes.AuthorityStartController.onPageLoad(NormalMode).url
        }
      }

      "user answers exists in CheckMode" in new Setup {

        val mockSessionRepository: SessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application: Application =
          applicationBuilder(userAnswers = Some(userAnswersCompanyDetails))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(authStartCheckModeRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[AccountsCacheService].toInstance(mockAccountsCacheService),
              bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService),
              bind[AuthorisedAccountsService].toInstance(mockAuthorisedAccountService)
            ).build()

        running(application) {
          val request = fakeRequest(POST, accountsSubmitRouteInCheckMode)
            .withFormUrlEncodedBody(("value[0]", answer.head))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.add.routes.AuthorityStartController.onPageLoad(CheckMode).url
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
          val request = fakeRequest(POST, accountsRoute)
            .withFormUrlEncodedBody(("value[0]", answer.head))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.add.routes.EoriNumberController.onPageLoad(NormalMode).url
        }
      }

      "user answers doesn't exist" in new Setup {

        val mockSessionRepository: SessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = None)
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[AccountsCacheService].toInstance(mockAccountsCacheService)
            ).build()

        running(application) {

          val request = fakeRequest(POST, accountsRoute)
            .withFormUrlEncodedBody(("value[0]", answer.head))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
        }
      }

    }

    "return a Bad Request and errors when invalid data is submitted" in new Setup {
      val application: Application = applicationBuilder(userAnswers = Some(userAnswersCompanyDetails))
        .overrides(
          bind[AccountsCacheService].toInstance(mockAccountsCacheService),
          bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService),
          bind[AuthorisedAccountsService].toInstance(mockAuthorisedAccountService)
        ).build()

      running(application) {

        val request = fakeRequest(POST, accountsRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AccountsView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(
            boundForm,
            AuthorisedAccounts(Seq.empty,
              answerAccounts,
              Seq(CashAccount("23456", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(bigDecimal100)))),
              Seq.empty,
              "GB9876543210000"),
            NormalMode,
            backLinkRoute)(request, messages(application), appConfig).toString
      }
    }
  }

  trait Setup {
    val amount100 = 100
    val bigDecimal100 = 100.00

    def onwardRoute: Call = Call("GET", "/foo")

    lazy val authStartNormalModeRoute: Call = controllers.add.routes.AuthorityStartController.onPageLoad(NormalMode)
    lazy val authStartCheckModeRoute: Call = controllers.add.routes.AuthorityStartController.onPageLoad(CheckMode)

    lazy val accountsRoute: String = controllers.add.routes.AccountsController.onPageLoad(NormalMode).url
    lazy val accountsRouteInCheckMode: String = controllers.add.routes.AccountsController.onPageLoad(CheckMode).url

    lazy val accountsSubmitRouteInNormalMode: String = controllers.add.routes.AccountsController.onSubmit(NormalMode).url
    lazy val accountsSubmitRouteInCheckMode: String = controllers.add.routes.AccountsController.onSubmit(CheckMode).url

    val formProvider = new AccountsFormProvider()
    val form: Form[List[String]] = formProvider()

    val userAnswersCompanyDetails: UserAnswers = emptyUserAnswers.set(
      EoriNumberPage, CompanyDetails("GB9876543210000", Some("name"))).success.value

    val userAnswersCompanyDetailsXI: UserAnswers = emptyUserAnswers.set(
      EoriNumberPage, CompanyDetails("XI9876543210000", Some("name"))).success.value

    val standingAuthority: StandingAuthority = StandingAuthority(
      "EORI",
      LocalDate.parse("2020-03-01"),
      Some(LocalDate.parse("2020-04-01")),
      viewBalance = false)

    val answer: List[String] = List("account_0")

    val answerAccounts: List[CashAccount] =
      List(CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(bigDecimal100))))

    val accounts: CDSAccounts =
      CDSAccounts(
        "GB123456789012",
        List(DutyDefermentAccount(
          "123",
          "XI9876543210000",
          AccountStatusOpen,
          DutyDefermentBalance(Some(bigDecimal100), Some(bigDecimal100), Some(bigDecimal100), Some(bigDecimal100)),
          isNiAccount = true),
          CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(bigDecimal100))),
          CashAccount("23456", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(bigDecimal100)))
        ))

    val authorisedAccounts: List[CashAccount] = List(CashAccount("12345", "GB123456789012",
      AccountStatusOpen, CDSCashBalance(Some(amount100))))

    private val closedAccount = List(CashAccount("23456", "GB123456789012",
      AccountStatusClosed, CDSCashBalance(Some(amount100))))

    val closedAccountXI: List[DutyDefermentAccount] =
      List(
        DutyDefermentAccount(
          "123",
          "XI9876543210000",
          AccountStatusOpen,
          DutyDefermentBalance(Some(bigDecimal100), Some(bigDecimal100), Some(bigDecimal100), Some(bigDecimal100)),
          isNiAccount = true)
      )

    val mockAccountsCacheService: AccountsCacheService = mock[AccountsCacheService]
    val mockAuthoritiesCacheService: AuthoritiesCacheService = mock[AuthoritiesCacheService]
    val mockAuthorisedAccountService: AuthorisedAccountsService = mock[AuthorisedAccountsService]

    when(mockAccountsCacheService.retrieveAccounts(any[InternalId](), any())(any()))
      .thenReturn(Future.successful(accounts))

    when(mockAuthorisedAccountService.getAuthorisedAccounts(any())(any(), any()))
      .thenReturn(Future.successful(AuthorisedAccounts(Seq.empty,
        authorisedAccounts, closedAccount, Seq.empty, "GB9876543210000")))

    val backLinkRoute: Call = controllers.add.routes.EoriDetailsCorrectController.onPageLoad(NormalMode)
    val backLinkRouteInCheckMode: Call = controllers.add.routes.AuthorisedUserController.onPageLoad()
  }
}
