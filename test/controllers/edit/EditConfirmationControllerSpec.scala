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

package controllers.edit

import base.SpecBase
import config.FrontendAppConfig
import models.domain._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.add.{AccountsPage, EoriNumberPage}
import pages.{ConfirmationDetails, ConfirmationPage}
import play.api.inject
import play.api.test.Helpers._
import repositories.{AccountsRepository, AuthoritiesRepository, SessionRepository}
import services.ConfirmationService
import views.html.edit.EditConfirmationView
import java.time.LocalDate
import models.CompanyDetails
import scala.concurrent.Future

class EditConfirmationControllerSpec extends SpecBase {

  val cashAccount = CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))
  val dutyDeferment = DutyDefermentAccount("67890", "GB210987654321", AccountStatusOpen, DutyDefermentBalance(None, None, None, None))
  val standingAuthority = StandingAuthority("GB123456789012", LocalDate.now(), None, viewBalance = true)

  val accountsWithAuthoritiesWithId = AccountWithAuthoritiesWithId(CdsCashAccount, "12345", Some(AccountStatusOpen), Map("b" -> standingAuthority))
  val authoritiesWithId: AuthoritiesWithId = AuthoritiesWithId(Map(
    ("a" -> accountsWithAuthoritiesWithId)
  ))

  "EditConfirmation Controller" must {
    "return OK and the correct view for a GET" when {

      "The user is returning to the page " in {/*
        val userAnswers = emptyUserAnswers.set(ConfirmationPage , ConfirmationDetails("eori", None, Some("Company Name"), false)).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).configure("features.edit-journey" -> true).build()
        running(application){
          val request = fakeRequest(GET, controllers.edit.routes.EditConfirmationController.onPageLoad("a", "b").url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[EditConfirmationView]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view("eori", None, Some("Company Name"))(request, messages(application), appConfig).toString
        }*/
      }

     "Start date is today with single account selected" in {

        val mockSessionRepository = mock[SessionRepository]
        val mockAccountsRepository = mock[AccountsRepository]
        val mockAuthoritiesRepository = mock[AuthoritiesRepository]
        val mockConfirmationService = mock[ConfirmationService]

        when(mockSessionRepository.clear("id")).thenReturn(Future.successful(true))
        when(mockAccountsRepository.clear("id")).thenReturn(Future.successful(true))
        when(mockAuthoritiesRepository.clear("id")).thenReturn(Future.successful(true))
        when(mockAuthoritiesRepository.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))
        when(mockConfirmationService.populateConfirmation(any(), any(), any(), any(), any())).thenReturn(Future.successful(true))

        val userAnswers = emptyUserAnswers
          .set(EoriNumberPage, CompanyDetails("GB123456789012", Some("Tony Stark"))).success.value
          .set(AccountsPage, List(cashAccount)).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository),
          inject.bind[AccountsRepository].toInstance(mockAccountsRepository),
          inject.bind[AuthoritiesRepository].toInstance(mockAuthoritiesRepository),
          inject.bind[ConfirmationService].toInstance(mockConfirmationService)
        ).configure("features.edit-journey" -> true).build()

        running(application) {

          val request = fakeRequest(GET,
            controllers.edit.routes.EditConfirmationController.onPageLoad(
              "a", "b").url)

          val result = route(application, request).value
          val view = application.injector.instanceOf[EditConfirmationView]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          status(result) mustEqual OK

          verify(mockSessionRepository, times(1)).clear("id")
          verify(mockAccountsRepository, times(1)).clear("id")
          verify(mockAuthoritiesRepository, times(1)).clear("id")

        //  contentAsString(result) mustEqual view("GB123456789012", None, Some("Tony Stark"))(request, messages(application), appConfig).toString
        }
      }
    }

    "redirect to session expired if EORI number is missing" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).configure("features.edit-journey" -> true).build()

      running(application) {

        val request = fakeRequest(GET, controllers.edit.routes.EditConfirmationController.onPageLoad("a", "b").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
      }
    }
  }
}
