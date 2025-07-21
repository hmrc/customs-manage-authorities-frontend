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
import models.{CompanyDetails, UserAnswers}
import models.domain._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.add.{AccountsPage, AuthorityStartDatePage, EoriNumberPage}
import play.api.{Application, inject}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{AccountAndAuthority, AuthoritiesCacheService, NoAuthority}
import utils.StringUtils.emptyString

import java.time.LocalDate
import scala.concurrent.Future

class ViewAuthorityControllerSpec extends SpecBase {

  "onPageLoad" must {
    "return 404 when calling with empty values" in new Setup {

      running(application) {
        val request = FakeRequest(
          GET,
          controllers.routes.ViewAuthorityController.onPageLoad(emptyString, emptyString).url
        )

        val result = route(application, request).value
        status(result) mustEqual NOT_FOUND
      }
    }

    "return OK when called with correct values" in new Setup {

      override val application: Application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          inject.bind[AuthoritiesCacheService].toInstance(mockAuthCacheService)
        )
        .build()

      when(mockAuthCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthority))))

      running(application) {
        val request =
          FakeRequest(GET, controllers.routes.ViewAuthorityController.onPageLoad(accountId, authorityId).url)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "return Ok when userAnswers is None" in new Setup {
      override val application: Application = applicationBuilder(userAnswers = None)
        .overrides(
          inject.bind[AuthoritiesCacheService].toInstance(mockAuthCacheService)
        )
        .build()

      when(mockAuthCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthority))))

      running(application) {
        val request =
          FakeRequest(GET, controllers.routes.ViewAuthorityController.onPageLoad(accountId, authorityId).url)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "return 303 when AuthoritiesCacheErrorResponse is received" in new Setup {

      override val application: Application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          inject.bind[AuthoritiesCacheService].toInstance(mockAuthCacheService)
        )
        .build()

      when(mockAuthCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Left(NoAuthority)))

      running(application) {
        val request =
          FakeRequest(GET, controllers.routes.ViewAuthorityController.onPageLoad(accountId, authorityId).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }
  }

  trait Setup {

    val oneMonth = 1

    val startDate: LocalDate = LocalDate.now().plusMonths(oneMonth)

    val cashAccount: CashAccount =
      CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))

    val dutyDeferment: DutyDefermentAccount =
      DutyDefermentAccount("67890", "GB210987654321", AccountStatusOpen, DutyDefermentBalance(None, None, None, None))

    val userAnswers: UserAnswers = emptyUserAnswers
      .set(EoriNumberPage, CompanyDetails("GB123456789012", Some("Company Name")))
      .success
      .value
      .set(AuthorityStartDatePage, startDate)
      .success
      .value
      .set(AccountsPage, List(cashAccount, dutyDeferment))
      .success
      .value

    val stDate: LocalDate  = LocalDate.parse("2020-03-01")
    val endDate: LocalDate = LocalDate.parse("2020-04-01")

    val accountId   = "test_account"
    val authorityId = "test_id"

    val standingAuthority: StandingAuthority = StandingAuthority("EORI", startDate, Some(endDate), viewBalance = false)

    val accountsWithAuthoritiesWithId: AccountWithAuthoritiesWithId =
      AccountWithAuthoritiesWithId(CdsCashAccount, "12345", Some(AccountStatusOpen), Map("b" -> standingAuthority))

    val mockAuthCacheService: AuthoritiesCacheService = mock[AuthoritiesCacheService]

    val application: Application = applicationBuilder(userAnswers = Some(userAnswers)).build()
  }
}
