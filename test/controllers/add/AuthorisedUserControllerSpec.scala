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
import config.FrontendAppConfig
import connectors.{CustomsDataStoreConnector, CustomsFinancialsConnector}
import controllers.actions.{FakeVerifyAccountNumbersAction, VerifyAccountNumbersAction}
import forms.AuthorisedUserFormProviderWithConsent
import models.domain._
import models.requests.Accounts
import models.{AuthorityStart, CompanyDetails, EoriDetailsCorrect, ShowBalance, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.mockito.{ArgumentMatchers, Mockito}
import org.scalatestplus.mockito.MockitoSugar
import pages.add._
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.guice.GuiceableModule
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, inject}
import services.DateTimeService
import services.add.CheckYourAnswersValidationService
import utils.StringUtils.emptyString
import views.html.add.AuthorisedUserView

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Future

class AuthorisedUserControllerSpec extends SpecBase with MockitoSugar {

  "onPageLoad" must {

    "return OK and the correct view for a GET" in new SetUp {

      val userAnswers: UserAnswers = userAnswersTodayToIndefinite.set(AccountsPage, List(cashAccount)).success.value
      val app: Application = applicationWithUserAnswersAndEori(userAnswers)

      when(mockValidator.validate(userAnswers)).thenReturn(Some((accounts, standingAuthority, authorisedUser)))

      running(app) {
        val view = app.injector.instanceOf[AuthorisedUserView]
        implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
        implicit val msg: Messages = messages(app)

        val helper = viewmodels.CheckYourAnswersHelper(userAnswers, mockDateTimeService)(messages(app))

        implicit val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest(GET, authorisedUserRoute)
        val result = route(app, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, helper).toString
      }
    }

    "return SEE_OTHER when CheckYourAnswersValidationService validate returns None on onPageLoad" in new SetUp {

      val userAnswers: UserAnswers = userAnswersTodayToIndefinite.set(AccountsPage, List(cashAccount)).success.value
      val app: Application = applicationWithUserAnswersAndEori(userAnswers)

      when(mockValidator.validate(userAnswers)).thenReturn(None)

      running(app) {
        val request = fakeRequest(GET, authorisedUserRoute)
        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "redirect to Session Expired for a GET if no existing data is found" in new SetUp {

      val app: Application = applicationBuilder(userAnswers = None).build()

      running(app) {
        val request = fakeRequest(GET, authorisedUserRoute)
        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual sessionExpiredPage
      }
    }

    "redirect to Session Expired for a POST if no existing data is found" in new SetUp {

      val app: Application = applicationBuilder(userAnswers = None).build()

      running(app) {
        val request = fakeRequest(POST, authorisedUserRoute).withFormUrlEncodedBody(("value", "answer"))
        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual sessionExpiredPage
      }
    }

  }

  "onSubmit" must {

    "redirect to next page for valid data" in new SetUp {
      val userAnswers: UserAnswers = userAnswersTodayToIndefinite.set(AccountsPage, List(cashAccount)).success.value
      val app: Application = applicationWithUserAnswersAndEori(
        userAnswers, dataStoreConnector = Some(mockDataStoreConnector))

      when(mockValidator.validate(userAnswers)).thenReturn(Some((accounts, standingAuthority, authorisedUser)))
      when(mockDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some(xiEori)))
      when(mockConnector.grantAccountAuthorities(any, any)(any)).thenReturn(Future.successful(true))

      running(app) {
        val request = fakeRequest(POST, onSubmitRoute)
        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "redirect to next page for valid data when user has selected cash,guarantee and DD accounts " +
      "and input EORI is XI EORI" in new SetUp {
      val userAnswers: UserAnswers = userAnswersTodayToIndefiniteWithXIEori.set(
        AccountsPage, List(cashAccount, generalGuarantee, dutyDeferment)).success.value

      val app: Application = applicationWithUserAnswersAndEori(userAnswers, gbEori, Some(mockDataStoreConnector))

      when(mockValidator.validate(userAnswers)).thenReturn(
        Some((accountsWithDDCashAndGuarantee, standingAuthority, authorisedUser)))
      when(mockDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some(xiEori)))

      when(mockConnector.grantAccountAuthorities(
        any, ArgumentMatchers.eq(xiEori))(any)).thenReturn(Future.successful(true))
      when(mockConnector.grantAccountAuthorities(
        any, ArgumentMatchers.eq(gbEori))(any)).thenReturn(Future.successful(true))

      running(app) {
        val request = fakeRequest(POST, onSubmitRoute)
        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        verify(mockConnector, Mockito.times(2)).grantAccountAuthorities(any, any)(any)
      }
    }

    "redirect to error page for valid data when user has selected cash,guarantee and DD accounts " +
      "and input EORI is XI EORI but grant authority calls fail" in new SetUp {
      val userAnswers: UserAnswers = userAnswersTodayToIndefiniteWithXIEori.set(
        AccountsPage, List(cashAccount, generalGuarantee, dutyDeferment)).success.value

      val app: Application = applicationWithUserAnswersAndEori(userAnswers, gbEori, Some(mockDataStoreConnector))

      when(mockValidator.validate(userAnswers)).thenReturn(
        Some((accountsWithDDCashAndGuarantee, standingAuthority, authorisedUser)))
      when(mockDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some(xiEori)))

      when(mockConnector.grantAccountAuthorities(
        any, ArgumentMatchers.eq(xiEori))(any)).thenReturn(Future.successful(false))
      when(mockConnector.grantAccountAuthorities(
        any, ArgumentMatchers.eq(gbEori))(any)).thenReturn(Future.successful(false))

      running(app) {
        val request = fakeRequest(POST, onSubmitRoute)
        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(technicalDifficultiesPage)

        verify(mockConnector, Mockito.times(2)).grantAccountAuthorities(any, any)(any)
      }
    }

    "redirect to error page for valid data when user has selected cash,guarantee and DD accounts " +
      "and input EORI is XI EORI but one of grant authority call fails" in new SetUp {
      val userAnswers: UserAnswers = userAnswersTodayToIndefiniteWithXIEori.set(
        AccountsPage, List(cashAccount, generalGuarantee, dutyDeferment)).success.value

      val app: Application = applicationWithUserAnswersAndEori(userAnswers, gbEori, Some(mockDataStoreConnector))

      when(mockValidator.validate(userAnswers)).thenReturn(
        Some((accountsWithDDCashAndGuarantee, standingAuthority, authorisedUser)))
      when(mockDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some(xiEori)))

      when(mockConnector.grantAccountAuthorities(
        any, ArgumentMatchers.eq(xiEori))(any)).thenReturn(Future.successful(true))
      when(mockConnector.grantAccountAuthorities(
        any, ArgumentMatchers.eq(gbEori))(any)).thenReturn(Future.successful(false))

      running(app) {
        val request = fakeRequest(POST, onSubmitRoute)
        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(technicalDifficultiesPage)

        verify(mockConnector, Mockito.times(2)).grantAccountAuthorities(any, any)(any)
      }
    }

    "redirect to next page for valid data when user has selected cash,guarantee and DD accounts " +
      "and input EORI is GB EORI" in new SetUp {
      val userAnswers: UserAnswers = userAnswersTodayToIndefinite.set(
        AccountsPage, List(cashAccount, generalGuarantee, dutyDeferment)).success.value

      val app: Application = applicationWithUserAnswersAndEori(
        userAnswers, dataStoreConnector = Some(mockDataStoreConnector))

      when(mockValidator.validate(userAnswers)).thenReturn(
        Some((accountsWithDDCashAndGuarantee, standingAuthority, authorisedUser)))
      when(mockDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some(xiEori)))
      when(mockConnector.grantAccountAuthorities(
        any, ArgumentMatchers.eq(gbEori))(any)).thenReturn(Future.successful(true))

      running(app) {
        val request = fakeRequest(POST, onSubmitRoute)
        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockConnector, Mockito.times(1)).grantAccountAuthorities(any, any)(any)
      }
    }

    "redirect to TechnicalDifficulties page for invalid data" in new SetUp {
      val userAnswers: UserAnswers = userAnswersTodayToIndefinite.set(AccountsPage, List(cashAccount)).success.value
      val app: Application = applicationWithUserAnswersAndEori(
        userAnswers, dataStoreConnector = Some(mockDataStoreConnector))

      when(mockValidator.validate(userAnswers)).thenReturn(Some((accounts, standingAuthority, authorisedUser)))
      when(mockDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some(xiEori)))
      when(mockConnector.grantAccountAuthorities(any, any)(any)).thenReturn(Future.successful(false))

      running(app) {
        val request = fakeRequest(POST, onSubmitRoute)
        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(technicalDifficultiesPage)
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

    val gbEori = "GB123456789012"
    val xiEori = "XI9876543210000"
    val accounts: Accounts = Accounts(
      Some(AccountWithAuthorities(CdsCashAccount, "12345", Some(AccountStatusOpen), Seq.empty)), Seq.empty, None)

    val accountsWithDDCashAndGuarantee: Accounts = Accounts(
      Some(AccountWithAuthorities(CdsCashAccount, "12345", Some(AccountStatusOpen), Seq.empty)),
      Seq("123456"),
      Some("123456"))

    val standingAuthority: StandingAuthority = StandingAuthority(gbEori,
      LocalDate.now(), Option(LocalDate.now().plusDays(1)), viewBalance = true)

    val authorisedUser: AuthorisedUser = AuthorisedUser("name", "role")
    val mockValidator: CheckYourAnswersValidationService = mock[CheckYourAnswersValidationService]
    val mockDateTimeService: DateTimeService = mock[DateTimeService]

    val cashAccount: CashAccount =
      CashAccount("12345", gbEori, AccountStatusOpen, CDSCashBalance(Some(100.00)))
    val dutyDeferment: DutyDefermentAccount =
      DutyDefermentAccount("67890", "GB210987654321", AccountStatusOpen, DutyDefermentBalance(None, None, None, None))
    val generalGuarantee: GeneralGuaranteeAccount =
      GeneralGuaranteeAccount("54321", "GB000000000000", AccountStatusOpen, Some(GeneralGuaranteeBalance(50.00, 50.00)))

    val selectedAccounts: List[CDSAccount] = List(cashAccount, dutyDeferment, generalGuarantee)

    val userAnswersTodayToIndefinite: UserAnswers =
      UserAnswers("id").set(AccountsPage, selectedAccounts).success.value
      .set(EoriNumberPage, CompanyDetails(gbEori, Some("companyName"))).success.value
      .set(AuthorityStartPage, AuthorityStart.Today)(AuthorityStart.writes).success.value
      .set(EoriDetailsCorrectPage, EoriDetailsCorrect.Yes)(EoriDetailsCorrect.writes).success.value
      .set(ShowBalancePage, ShowBalance.Yes)(ShowBalance.writes).success.value
      .set(AuthorityDetailsPage, AuthorisedUser(emptyString, emptyString)).success.value

    val userAnswersTodayToIndefiniteWithXIEori: UserAnswers =
      UserAnswers("id").set(AccountsPage, selectedAccounts).success.value
      .set(EoriNumberPage, CompanyDetails(xiEori, Some("companyName"))).success.value
      .set(AuthorityStartPage, AuthorityStart.Today)(AuthorityStart.writes).success.value
      .set(EoriDetailsCorrectPage, EoriDetailsCorrect.Yes)(EoriDetailsCorrect.writes).success.value
      .set(ShowBalancePage, ShowBalance.Yes)(ShowBalance.writes).success.value
      .set(AuthorityDetailsPage, AuthorisedUser(emptyString, emptyString)).success.value

    val technicalDifficultiesPage: String = controllers.routes.TechnicalDifficulties.onPageLoad.url
    val sessionExpiredPage: String = controllers.routes.SessionExpiredController.onPageLoad.url

    when(mockConnector.grantAccountAuthorities(any(), any())(any())).thenReturn(Future.successful(true))
    when(mockValidator.validate(any())).thenReturn(Some((accounts, standingAuthority, authorisedUser)))
    when(mockDateTimeService.localTime()).thenReturn(LocalDateTime.now())

    when(mockConnector.grantAccountAuthorities(any(), any())(any())).thenReturn(Future.successful(true))
    when(mockValidator.validate(userAnswersTodayToIndefinite))
      .thenReturn(Some((accounts, standingAuthority, authorisedUser)))

    when(mockDateTimeService.localTime()).thenReturn(LocalDateTime.now())

    def applicationWithUserAnswersAndEori(userAnswer: UserAnswers = emptyUserAnswers,
                                          requestEori: String = emptyString,
                                          dataStoreConnector: Option[CustomsDataStoreConnector] = None): Application = {
      val moduleList: Seq[GuiceableModule] =
        if (dataStoreConnector.isEmpty) {
          Seq(
            inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
            inject.bind[CheckYourAnswersValidationService].toInstance(mockValidator),
            inject.bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswer))
          )
        } else {
          Seq(
            inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
            inject.bind[CheckYourAnswersValidationService].toInstance(mockValidator),
            inject.bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswer)),
            inject.bind[CustomsDataStoreConnector].toInstance(dataStoreConnector.get)
          )
        }

      applicationBuilder(Some(userAnswer), requestEori).overrides(moduleList: _*).build()
    }
  }
}
