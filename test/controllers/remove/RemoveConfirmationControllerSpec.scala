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

package controllers.remove

import base.SpecBase
import models.domain._
import models.CompanyDetails
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.add.{AccountsPage, EoriNumberPage}
import play.api.inject
import play.api.inject.bind
import play.api.test.Helpers._
import repositories.AuthoritiesRepository
import services.ConfirmationService
import java.time.LocalDate
import scala.concurrent.Future

class RemoveConfirmationControllerSpec extends SpecBase {

  val startDate = LocalDate.parse("2020-03-01")
  val endDate = LocalDate.parse("2020-04-01")
  val standingAuthority = StandingAuthority("EORI", startDate, Some(endDate), viewBalance = false)
  val accounts = Seq(AccountWithAuthorities(CdsCashAccount, "12345", Some(AccountStatusOpen), Seq(standingAuthority)))
  val cashAccount = CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))


  val authoritiesWithId: AuthoritiesWithId = AuthoritiesWithId(Map(
    ("a" -> AccountWithAuthoritiesWithId(CdsCashAccount, "12345",
      Some(AccountStatusOpen), Map("b" -> standingAuthority)))))

  "RemoveConfirmation Controller" must {
    "throw an exception" when {
      "accountId cannot be found" in {

        val mockRepository = mock[AuthoritiesRepository]
        when(mockRepository.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))

        val application = applicationBuilder()
          .overrides(bind[AuthoritiesRepository].toInstance(mockRepository)).build()

        running(application) {

          val request = fakeRequest(GET, controllers.remove.routes.RemoveConfirmationController.onPageLoad("missing", "b").url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
        }
      }

      "authorityId cannot be found" in {
        val mockRepository = mock[AuthoritiesRepository]
        when(mockRepository.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))

        val application = applicationBuilder()
          .overrides(bind[AuthoritiesRepository].toInstance(mockRepository)).build()

        running(application) {

          val request = fakeRequest(GET, controllers.remove.routes.RemoveConfirmationController.onPageLoad("a", "missing").url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
        }
      }

      "return Ok for a GET" in {
        val mockAuthoritiesRepository = mock[AuthoritiesRepository]
        val mockConfirmationService = mock[ConfirmationService]

        when(mockAuthoritiesRepository.clear("id")).thenReturn(Future.successful(true))
        when(mockAuthoritiesRepository.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))
        when(mockConfirmationService.populateConfirmation(any(), any(), any(), any(), any())).thenReturn(Future.successful(true))

        val userAnswers = emptyUserAnswers
          .set(EoriNumberPage, CompanyDetails("GB123456789012", Some("Tony Stark"))).success.value
          .set(AccountsPage, List(cashAccount)).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
          inject.bind[AuthoritiesRepository].toInstance(mockAuthoritiesRepository),
          inject.bind[ConfirmationService].toInstance(mockConfirmationService)
        ).configure("features.edit-journey" -> true).build()

        running(application) {

          val request = fakeRequest(GET,
            controllers.remove.routes.RemoveConfirmationController.onPageLoad(
              "a", "b").url)

          val result = route(application, request).value

          status(result) mustEqual OK
          verify(mockAuthoritiesRepository, times(1)).clear("id")
        }
      }
    }
  }
}
