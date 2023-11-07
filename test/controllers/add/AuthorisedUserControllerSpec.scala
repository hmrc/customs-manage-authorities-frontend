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
import controllers.actions.{FakeVerifyAccountNumbersAction, VerifyAccountNumbersAction}
import forms.AuthorisedUserFormProviderWithConsent
import models.domain._
import models.requests.Accounts
import models.{AuthorityStart, CompanyDetails, EoriDetailsCorrect, ShowBalance, UserAnswers}
import org.mockito.{ArgumentMatchers, Mockito}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add._
import play.api.data.Form
import play.api.test.Helpers._
import play.api.{Application, inject}
import services.DateTimeService
import services.add.CheckYourAnswersValidationService
import views.html.add.AuthorisedUserView

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Future

class AuthorisedUserControllerSpec extends SpecBase with MockitoSugar {

  "onPageLoad" must {

    "return OK and the correct view for a GET" in new SetUp {

      val userAnswers: UserAnswers = userAnswersTodayToIndefinite.set(AccountsPage, List(cashAccount)).success.value

      when(mockValidator.validate(userAnswers)).thenReturn(Some((accounts, standingAuthority, authorisedUser)))

      val application: Application = applicationBuilder(Some(userAnswers))
        .overrides(
          inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
          inject.bind[CheckYourAnswersValidationService].toInstance(mockValidator),
          inject.bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers))
        ).build()

      running(application) {

        val request = fakeRequest(GET, authorisedUserRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[AuthorisedUserView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val helper = viewmodels.CheckYourAnswersHelper(userAnswers, mockDateTimeService)(messages(application))

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, helper)(request, messages(application), appConfig).toString
      }
    }

    "return SEE_OTHER when CheckYourAnswersValidationService validate returns None on onPageLoad" in new SetUp {

      val userAnswers: UserAnswers = userAnswersTodayToIndefinite.set(AccountsPage, List(cashAccount)).success.value

      when(mockValidator.validate(userAnswers)).thenReturn(None)

      val application: Application = applicationBuilder(Some(userAnswers))
        .overrides(
          inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
          inject.bind[CheckYourAnswersValidationService].toInstance(mockValidator),
          inject.bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers))
        ).build()

      running(application) {
        val request = fakeRequest(GET, authorisedUserRoute)
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
      }
    }

    "redirect to Session Expired for a GET if no existing data is found" in new SetUp {

      val application: Application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = fakeRequest(GET, authorisedUserRoute)
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
      }
    }

    "redirect to Session Expired for a POST if no existing data is found" in new SetUp {

      val application: Application = applicationBuilder(userAnswers = None).build()

      running(application) {

        val request =
          fakeRequest(POST, authorisedUserRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
      }
    }

  }

  "onSubmit" must {
    "redirect to next page for valid data" in new SetUp {
      val userAnswers: UserAnswers = userAnswersTodayToIndefinite.set(AccountsPage, List(cashAccount)).success.value

      when(mockValidator.validate(userAnswers)).thenReturn(Some((accounts, standingAuthority, authorisedUser)))
      when(mockDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some("XI9876543210000")))
      when(mockConnector.grantAccountAuthorities(any, any)(any)).thenReturn(Future.successful(true))

      val application: Application = applicationBuilder(Some(userAnswers))
        .overrides(
          inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
          inject.bind[CheckYourAnswersValidationService].toInstance(mockValidator),
          inject.bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers)),
          inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector)
        ).build()

      running(application) {
        val request = fakeRequest(POST, onSubmitRoute)
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
      }
    }

    "redirect to next page for valid data when user has selected cash,guarantee and DD accounts " +
      "and input EORI is XI EORI" in new SetUp {
      val userAnswers: UserAnswers = userAnswersTodayToIndefiniteWithXIEori.set(
        AccountsPage, List(cashAccount, generalGuarantee, dutyDeferment)).success.value

      when(mockValidator.validate(userAnswers)).thenReturn(
        Some((accountsWithDDCashAndGuarantee, standingAuthority, authorisedUser)))
      when(mockDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some("XI9876543210000")))

      when(mockConnector.grantAccountAuthorities(
        any, ArgumentMatchers.eq("XI9876543210000"))(any)).thenReturn(Future.successful(true))
      when(mockConnector.grantAccountAuthorities(
        any, ArgumentMatchers.eq("GB123456789012"))(any)).thenReturn(Future.successful(true))

      val application: Application = applicationBuilder(Some(userAnswers), "GB123456789012")
        .overrides(
          inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
          inject.bind[CheckYourAnswersValidationService].toInstance(mockValidator),
          inject.bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers)),
          inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector)
        ).build()

      running(application) {
        val request = fakeRequest(POST, onSubmitRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(mockConnector, Mockito.times(2)).grantAccountAuthorities(any, any)(any)
      }
    }

    "redirect to error page for valid data when user has selected cash,guarantee and DD accounts " +
      "and input EORI is XI EORI but grant authority calls fail" in new SetUp {
      val userAnswers: UserAnswers = userAnswersTodayToIndefiniteWithXIEori.set(
        AccountsPage, List(cashAccount, generalGuarantee, dutyDeferment)).success.value

      when(mockValidator.validate(userAnswers)).thenReturn(
        Some((accountsWithDDCashAndGuarantee, standingAuthority, authorisedUser)))
      when(mockDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some("XI9876543210000")))

      when(mockConnector.grantAccountAuthorities(
        any, ArgumentMatchers.eq("XI9876543210000"))(any)).thenReturn(Future.successful(false))
      when(mockConnector.grantAccountAuthorities(
        any, ArgumentMatchers.eq("GB123456789012"))(any)).thenReturn(Future.successful(false))

      val application: Application = applicationBuilder(Some(userAnswers), "GB123456789012")
        .overrides(
          inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
          inject.bind[CheckYourAnswersValidationService].toInstance(mockValidator),
          inject.bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers)),
          inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector)
        ).build()

      running(application) {
        val request = fakeRequest(POST, onSubmitRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.TechnicalDifficulties.onPageLoad.url)

        verify(mockConnector, Mockito.times(2)).grantAccountAuthorities(any, any)(any)
      }
    }

    "redirect to error page for valid data when user has selected cash,guarantee and DD accounts " +
      "and input EORI is XI EORI but one of grant authority call fails" in new SetUp {
      val userAnswers: UserAnswers = userAnswersTodayToIndefiniteWithXIEori.set(
        AccountsPage, List(cashAccount, generalGuarantee, dutyDeferment)).success.value

      when(mockValidator.validate(userAnswers)).thenReturn(
        Some((accountsWithDDCashAndGuarantee, standingAuthority, authorisedUser)))
      when(mockDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some("XI9876543210000")))

      when(mockConnector.grantAccountAuthorities(
        any, ArgumentMatchers.eq("XI9876543210000"))(any)).thenReturn(Future.successful(true))
      when(mockConnector.grantAccountAuthorities(
        any, ArgumentMatchers.eq("GB123456789012"))(any)).thenReturn(Future.successful(false))

      val application: Application = applicationBuilder(Some(userAnswers), "GB123456789012")
        .overrides(
          inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
          inject.bind[CheckYourAnswersValidationService].toInstance(mockValidator),
          inject.bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers)),
          inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector)
        ).build()

      running(application) {
        val request = fakeRequest(POST, onSubmitRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.TechnicalDifficulties.onPageLoad.url)

        verify(mockConnector, Mockito.times(2)).grantAccountAuthorities(any, any)(any)
      }
    }

    "redirect to next page for valid data when user has selected cash,guarantee and DD accounts " +
      "and input EORI is GB EORI" in new SetUp {
      val userAnswers: UserAnswers = userAnswersTodayToIndefinite.set(
        AccountsPage, List(cashAccount, generalGuarantee, dutyDeferment)).success.value

      when(mockValidator.validate(userAnswers)).thenReturn(
        Some((accountsWithDDCashAndGuarantee, standingAuthority, authorisedUser)))
      when(mockDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some("XI9876543210000")))
      when(mockConnector.grantAccountAuthorities(
        any, ArgumentMatchers.eq("GB123456789012"))(any)).thenReturn(Future.successful(true))

      val application: Application = applicationBuilder(Some(userAnswers))
        .overrides(
          inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
          inject.bind[CheckYourAnswersValidationService].toInstance(mockValidator),
          inject.bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers)),
          inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector)
        ).build()

      running(application) {
        val request = fakeRequest(POST, onSubmitRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockConnector, Mockito.times(1)).grantAccountAuthorities(any, any)(any)
      }
    }

    "redirect to TechnicalDifficulties page for invalid data" in new SetUp {
      val userAnswers: UserAnswers = userAnswersTodayToIndefinite.set(AccountsPage, List(cashAccount)).success.value

      when(mockValidator.validate(userAnswers)).thenReturn(Some((accounts, standingAuthority, authorisedUser)))
      when(mockDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some("XI9876543210000")))
      when(mockConnector.grantAccountAuthorities(any, any)(any)).thenReturn(Future.successful(false))

      val application: Application = applicationBuilder(Some(userAnswers))
        .overrides(
          inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
          inject.bind[CheckYourAnswersValidationService].toInstance(mockValidator),
          inject.bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers)),
          inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector)
        ).build()

      running(application) {
        val request = fakeRequest(POST, onSubmitRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.TechnicalDifficulties.onPageLoad.url)
      }
    }
  }

  trait SetUp {
    protected val formProvider = new AuthorisedUserFormProviderWithConsent()
    protected val form: Form[AuthorisedUser] = formProvider()


    lazy val authorisedUserRoute: String = controllers.add.routes.AuthorisedUserController.onPageLoad().url
    lazy val onSubmitRoute: String = controllers.add.routes.AuthorisedUserController.onSubmit().url

    val mockConnector: CustomsFinancialsConnector = mock[CustomsFinancialsConnector]
    val mockDataStoreConnector: CustomsDataStoreConnector = mock[CustomsDataStoreConnector]

    val accounts: Accounts = Accounts(
      Some(AccountWithAuthorities(CdsCashAccount, "12345", Some(AccountStatusOpen), Seq.empty)), Seq.empty, None)

    val accountsWithDDCashAndGuarantee: Accounts = Accounts(
      Some(AccountWithAuthorities(CdsCashAccount, "12345", Some(AccountStatusOpen), Seq.empty)),
      Seq("123456"),
      Some("123456"))

    val standingAuthority: StandingAuthority = StandingAuthority("GB123456789012",
      LocalDate.now(), Option(LocalDate.now().plusDays(1)), viewBalance = true)

    val authorisedUser: AuthorisedUser = AuthorisedUser("name", "role")
    val mockValidator: CheckYourAnswersValidationService = mock[CheckYourAnswersValidationService]
    val mockDateTimeService: DateTimeService = mock[DateTimeService]

    when(mockConnector.grantAccountAuthorities(any(), any())(any())).thenReturn(Future.successful(true))
    when(mockValidator.validate(any())).thenReturn(Some((accounts, standingAuthority, authorisedUser)))
    when(mockDateTimeService.localTime()).thenReturn(LocalDateTime.now())

    val cashAccount: CashAccount =
      CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))
    val dutyDeferment: DutyDefermentAccount =
      DutyDefermentAccount("67890", "GB210987654321", AccountStatusOpen, DutyDefermentBalance(None, None, None, None))
    val generalGuarantee: GeneralGuaranteeAccount =
      GeneralGuaranteeAccount("54321", "GB000000000000", AccountStatusOpen, Some(GeneralGuaranteeBalance(50.00, 50.00)))
    val selectedAccounts: List[CDSAccount] = List(cashAccount, dutyDeferment, generalGuarantee)

    val userAnswersTodayToIndefinite: UserAnswers = UserAnswers("id")
      .set(AccountsPage, selectedAccounts).success.value
      .set(EoriNumberPage, CompanyDetails("GB123456789012", Some("companyName"))).success.value
      .set(AuthorityStartPage, AuthorityStart.Today)(AuthorityStart.writes).success.value
      .set(EoriDetailsCorrectPage, EoriDetailsCorrect.Yes)(EoriDetailsCorrect.writes).success.value
      .set(ShowBalancePage, ShowBalance.Yes)(ShowBalance.writes).success.value
      .set(AuthorityDetailsPage, AuthorisedUser("", "")).success.value

    val userAnswersTodayToIndefiniteWithXIEori: UserAnswers = UserAnswers("id")
      .set(AccountsPage, selectedAccounts).success.value
      .set(EoriNumberPage, CompanyDetails("XI9876543210000", Some("companyName"))).success.value
      .set(AuthorityStartPage, AuthorityStart.Today)(AuthorityStart.writes).success.value
      .set(EoriDetailsCorrectPage, EoriDetailsCorrect.Yes)(EoriDetailsCorrect.writes).success.value
      .set(ShowBalancePage, ShowBalance.Yes)(ShowBalance.writes).success.value
      .set(AuthorityDetailsPage, AuthorisedUser("", "")).success.value

    when(mockConnector.grantAccountAuthorities(any(), any())(any())).thenReturn(Future.successful(true))
    when(mockValidator.validate(userAnswersTodayToIndefinite)).thenReturn(Some((accounts, standingAuthority, authorisedUser)))
    when(mockDateTimeService.localTime()).thenReturn(LocalDateTime.now())
  }
}
