/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.AuthorisedUserFormProviderWithConsent
import models.{AuthorityStart, CompanyDetails, EoriDetailsCorrect, ShowBalance, UserAnswers}
import models.domain._
import models.requests.Accounts
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.{AccountsPage, AuthorityDetailsPage, AuthorityStartPage, EoriDetailsCorrectPage, EoriNumberPage, ShowBalancePage}
import play.api.mvc.Call
import play.api.test.Helpers._
import services.DateTimeService
import services.add.CheckYourAnswersValidationService
import java.time.{LocalDate, LocalDateTime}
import play.api.i18n.Messages
import scala.concurrent.Future

class AuthorisedUserControllerSpec extends SpecBase with MockitoSugar {

  implicit val messages: Messages = messagesApi.preferred(fakeRequest())

  private val formProvider = new AuthorisedUserFormProviderWithConsent()
  private val form = formProvider()
  private def onwardRoute = Call("GET", "/foo")
  private lazy val authorisedUserRoute = controllers.add.routes.AuthorisedUserController.onPageLoad.url

  val mockConnector: CustomsFinancialsConnector = mock[CustomsFinancialsConnector]
  when(mockConnector.grantAccountAuthorities(any())(any())).thenReturn(Future.successful(true))

  val accounts: Accounts = Accounts(Some(AccountWithAuthorities(CdsCashAccount, "12345", Some(AccountStatusOpen), Seq.empty)), Seq.empty, None)
  val standingAuthority: StandingAuthority = StandingAuthority("GB123456789012", LocalDate.now(), Option(LocalDate.now().plusDays(1)), viewBalance = true)
  val authorisedUser: AuthorisedUser = AuthorisedUser("name", "role")
  val mockValidator: CheckYourAnswersValidationService = mock[CheckYourAnswersValidationService]
  when(mockValidator.validate(any())).thenReturn(Some((accounts, standingAuthority, authorisedUser)))

  val mockDateTimeService: DateTimeService = mock[DateTimeService]
  when(mockDateTimeService.localTime()).thenReturn(LocalDateTime.now())

  val cashAccount: CashAccount = CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))
  val dutyDeferment: DutyDefermentAccount = DutyDefermentAccount("67890", "GB210987654321", AccountStatusOpen, DutyDefermentBalance(None, None, None, None))
  val generalGuarantee: GeneralGuaranteeAccount = GeneralGuaranteeAccount("54321", "GB000000000000", AccountStatusOpen, Some(GeneralGuaranteeBalance(50.00, 50.00)))
  val selectedAccounts: List[CDSAccount] = List(cashAccount, dutyDeferment, generalGuarantee)

  val userAnswersTodayToIndefinite: UserAnswers = UserAnswers("id")
    .set(AccountsPage, selectedAccounts).success.value
    .set(EoriNumberPage, CompanyDetails("GB123456789012", Some("companyName"))).success.value
    .set(AuthorityStartPage, AuthorityStart.Today)(AuthorityStart.writes).success.value
    .set(EoriDetailsCorrectPage, EoriDetailsCorrect.Yes)(EoriDetailsCorrect.writes).success.value
    .set(ShowBalancePage, ShowBalance.Yes)(ShowBalance.writes).success.value
    .set(AuthorityDetailsPage, AuthorisedUser("", "")).success.value

  "AuthorisedUser Controller" must {

    /*"return OK and the correct view for a GET" in {

      val userAnswers = userAnswersTodayToIndefinite.set(AccountsPage, List(cashAccount)).success.value

      val application = applicationBuilder(Some(userAnswers))
        .overrides(
          bind[CustomsFinancialsConnector].toInstance(mockConnector),
          bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers))
        )
        .build()

      running(application) {

        val request = fakeRequest(GET, authorisedUserRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AuthorisedUserView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val helper = CheckYourAnswersHelper(userAnswers, mockDateTimeService)(messages(application))

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, helper)(request, messages(application), appConfig).toString
      }
    }
*/
    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {

        val request = fakeRequest(GET, authorisedUserRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
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

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
      }
    }
  }
}
