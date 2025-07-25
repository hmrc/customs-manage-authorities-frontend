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

package controllers.remove

import base.SpecBase
import models.domain.*
import models.CompanyDetails
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.{ConfirmationDetails, ConfirmationPage}
import pages.add.{AccountsPage, EoriNumberPage}
import play.api.inject
import play.api.inject.bind
import play.api.test.Helpers.*
import repositories.AuthoritiesRepository
import services.ConfirmationService

import java.time.LocalDate
import scala.concurrent.Future

class RemoveConfirmationControllerSpec extends SpecBase {

  val startDate: LocalDate = LocalDate.parse("2020-03-01")
  val endDate: LocalDate   = LocalDate.parse("2020-04-01")

  val standingAuthority: StandingAuthority = StandingAuthority("EORI", startDate, Some(endDate), viewBalance = false)

  val accounts: Seq[AccountWithAuthorities] = Seq(
    AccountWithAuthorities(CdsCashAccount, "12345", Some(AccountStatusOpen), Seq(standingAuthority))
  )

  val cashAccount: CashAccount = CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))

  val authoritiesWithId: AuthoritiesWithId = AuthoritiesWithId(
    Map(
      "a" -> AccountWithAuthoritiesWithId(
        CdsCashAccount,
        "12345",
        Some(AccountStatusOpen),
        Map("b" -> standingAuthority)
      )
    )
  )

  "RemoveConfirmation Controller" must {
    "throw an exception" when {
      "accountId cannot be found" in {

        val mockRepository = mock[AuthoritiesRepository]
        when(mockRepository.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))

        val application = applicationBuilder()
          .overrides(bind[AuthoritiesRepository].toInstance(mockRepository))
          .build()

        running(application) {

          val request =
            fakeRequest(GET, controllers.remove.routes.RemoveConfirmationController.onPageLoad("missing", "b").url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
        }
      }

      "authorityId cannot be found" in {
        val mockRepository = mock[AuthoritiesRepository]
        when(mockRepository.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))

        val application = applicationBuilder()
          .overrides(bind[AuthoritiesRepository].toInstance(mockRepository))
          .build()

        running(application) {

          val request =
            fakeRequest(GET, controllers.remove.routes.RemoveConfirmationController.onPageLoad("a", "missing").url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
        }
      }

      "return Ok for a GET" in {
        val mockAuthoritiesRepository = mock[AuthoritiesRepository]
        val mockConfirmationService   = mock[ConfirmationService]

        when(mockAuthoritiesRepository.clear("id")).thenReturn(Future.successful(true))
        when(mockAuthoritiesRepository.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))
        when(mockConfirmationService.populateConfirmation(any(), any(), any(), any(), any()))
          .thenReturn(Future.successful(true))

        val userAnswers = emptyUserAnswers
          .set(EoriNumberPage, CompanyDetails("GB123456789012", Some("Tony Stark")))
          .success
          .value
          .set(AccountsPage, List(cashAccount))
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            inject.bind[AuthoritiesRepository].toInstance(mockAuthoritiesRepository),
            inject.bind[ConfirmationService].toInstance(mockConfirmationService)
          )
          .configure("features.edit-journey" -> true)
          .build()

        running(application) {

          val request =
            fakeRequest(GET, controllers.remove.routes.RemoveConfirmationController.onPageLoad("a", "b").url)

          val result = route(application, request).value

          status(result) mustEqual OK
          verify(mockAuthoritiesRepository, times(1)).clear("id")
        }
      }

      "recover using ConfirmationPage data if an exception is thrown" in {
        val mockRepository = mock[AuthoritiesRepository]
        when(mockRepository.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))
        when(mockRepository.clear(any())).thenThrow(new RuntimeException("Simulated failure"))

        val userAnswers = emptyUserAnswers
          .set(EoriNumberPage, CompanyDetails("GB123456789012", Some("Tony Stark")))
          .success
          .value
          .set(AccountsPage, List(cashAccount))
          .success
          .value
          .set(ConfirmationPage, ConfirmationDetails("GB123456789012", None, Some("Stark Industries"), false))
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[AuthoritiesRepository].toInstance(mockRepository))
          .build()

        running(application) {
          val request =
            fakeRequest(GET, controllers.remove.routes.RemoveConfirmationController.onPageLoad("a", "b").url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Stark Industries")
        }
      }

      "redirect to SessionExpired if ConfirmationPage is missing in userAnswers during recovery" in {
        val mockRepository = mock[AuthoritiesRepository]
        when(mockRepository.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))
        when(mockRepository.clear(any())).thenThrow(new RuntimeException("Simulated failure"))

        val userAnswers = emptyUserAnswers
          .set(EoriNumberPage, CompanyDetails("GB123456789012", Some("Tony Stark")))
          .success
          .value
          .set(AccountsPage, List(cashAccount))
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[AuthoritiesRepository].toInstance(mockRepository))
          .build()

        running(application) {
          val request =
            fakeRequest(GET, controllers.remove.routes.RemoveConfirmationController.onPageLoad("a", "b").url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
        }
      }

      "redirect to SessionExpired if userAnswers is completely missing during recovery" in {
        val mockRepository = mock[AuthoritiesRepository]
        when(mockRepository.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))
        when(mockRepository.clear(any())).thenThrow(new RuntimeException("Simulated failure"))

        val application = applicationBuilder(userAnswers = None)
          .overrides(bind[AuthoritiesRepository].toInstance(mockRepository))
          .build()

        running(application) {
          val request =
            fakeRequest(GET, controllers.remove.routes.RemoveConfirmationController.onPageLoad("a", "b").url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
        }
      }
    }

    "redirect correctly for GET" when {
      "error occurs during processing and ConfirmationPage value is present in userAnswers" in {
        val mockAuthoritiesRepository = mock[AuthoritiesRepository]
        val mockConfirmationService   = mock[ConfirmationService]

        when(mockAuthoritiesRepository.clear("id")).thenReturn(Future.successful(true))
        when(mockAuthoritiesRepository.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))
        when(mockConfirmationService.populateConfirmation(any(), any(), any(), any(), any()))
          .thenReturn(Future.failed(new RuntimeException("Error occurred")))

        val userAnswers = emptyUserAnswers
          .set(EoriNumberPage, CompanyDetails("GB123456789012", Some("Tony Stark")))
          .success
          .value
          .set(AccountsPage, List(cashAccount))
          .success
          .value
          .set(ConfirmationPage, ConfirmationDetails("eori", None, None, true))
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            inject.bind[AuthoritiesRepository].toInstance(mockAuthoritiesRepository),
            inject.bind[ConfirmationService].toInstance(mockConfirmationService)
          )
          .configure("features.edit-journey" -> true)
          .build()

        running(application) {

          val request =
            fakeRequest(GET, controllers.remove.routes.RemoveConfirmationController.onPageLoad("a", "b").url)

          val result = route(application, request).value

          status(result) mustEqual OK
          verify(mockAuthoritiesRepository, times(1)).clear("id")
        }
      }

      "error occurs during processing and ConfirmationPage value" +
        " is not present in userAnswers" in {
          val mockAuthoritiesRepository = mock[AuthoritiesRepository]
          val mockConfirmationService   = mock[ConfirmationService]

          when(mockAuthoritiesRepository.clear("id")).thenReturn(Future.successful(true))
          when(mockAuthoritiesRepository.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))
          when(mockConfirmationService.populateConfirmation(any(), any(), any(), any(), any()))
            .thenReturn(Future.failed(new RuntimeException("Error occurred")))

          val userAnswers = emptyUserAnswers
            .set(EoriNumberPage, CompanyDetails("GB123456789012", Some("Tony Stark")))
            .success
            .value
            .set(AccountsPage, List(cashAccount))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              inject.bind[AuthoritiesRepository].toInstance(mockAuthoritiesRepository),
              inject.bind[ConfirmationService].toInstance(mockConfirmationService)
            )
            .configure("features.edit-journey" -> true)
            .build()

          running(application) {

            val request =
              fakeRequest(GET, controllers.remove.routes.RemoveConfirmationController.onPageLoad("a", "b").url)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            verify(mockAuthoritiesRepository, times(1)).clear("id")
          }
        }
    }
  }
}
