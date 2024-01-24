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

package services

import base.SpecBase
import models.domain.{
  AccountStatusClosed, AccountStatusOpen, AccountWithAuthoritiesWithId, AuthoritiesWithId,
  CDSAccounts, CDSCashBalance, CashAccount, CdsCashAccount, EORI, StandingAuthority
}
import models.requests.DataRequest
import models.{AuthorisedAccounts, InternalId, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.add.AccountsPage
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Name}
import uk.gov.hmrc.http.HeaderCarrier
import utils.StringUtils.emptyString

import scala.concurrent.ExecutionContext.Implicits.global
import java.time.LocalDate
import scala.concurrent.Future

class AuthorisedAccountsServiceSpec extends SpecBase {

  "getAuthorisedAccounts" should {
    "return correct AuthorisedAccounts" in new Setup {
      val userAnswers: UserAnswers = emptyUserAnswers.set(AccountsPage, List.empty).success.value

      when(mockAuthCacheService.retrieveAuthorities(any, any)(any)).thenReturn(
        Future.successful(authoritiesWithId))

      when(mockAccCacheService.retrieveAccounts(any, any)(any)).thenReturn(
        Future.successful(accounts)
      )

      authorisedAccountsService.getAuthorisedAccounts(gbEori)(dataRequest(userAnswers, gbEori), hc).map {
        authAcc => authAcc mustBe authAccounts
      }
    }
  }

  "filterAccounts" should {
    "return correct CDSAccount for GB Eori" in new Setup {
      authorisedAccountsService.filterAccounts(gbEori, cdsAccounts) mustBe Seq(cashAccount)
    }

    "return correct CDSAccount for XI Eori" in new Setup {
      authorisedAccountsService.filterAccounts(xiEori, cdsAccounts) mustBe Seq(cashAccoiuntForNI)
    }
  }

  trait Setup {
    val xiEori = "XI098765432109"
    val gbEori = "GB098765432109"

    val cashAccount: CashAccount =
      CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))

    val cashAccoiuntForNI: CashAccount =
      CashAccount("54321", "GB098765432109", AccountStatusOpen, CDSCashBalance(Some(100.00)), isNiAccount = true)

    val cdsAccounts: Seq[CashAccount] = Seq(cashAccount, cashAccoiuntForNI)

    val openCashAccount: CashAccount =
      CashAccount("23456", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(100.00)))
    val closedCashAccount: CashAccount =
      CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))

    val accounts: CDSAccounts = CDSAccounts("GB123456789012", List(openCashAccount, closedCashAccount))

    val standingAuthority: StandingAuthority =
      StandingAuthority("GB123456789012", LocalDate.now(), None, viewBalance = true)

    val accountsWithAuthoritiesWithId: AccountWithAuthoritiesWithId = AccountWithAuthoritiesWithId(
      CdsCashAccount, "12345", Some(AccountStatusOpen), Map("b" -> standingAuthority))

    val authoritiesWithId: AuthoritiesWithId = AuthoritiesWithId(Map(
      ("a" -> accountsWithAuthoritiesWithId)
    ))

    val authAccounts: AuthorisedAccounts = AuthorisedAccounts(
      alreadyAuthorisedAccounts = Seq(openCashAccount),
      availableAccounts = Seq(openCashAccount),
      closedAccounts = Seq(),
      pendingAccounts = Seq(),
      enteredEori = "GB123456789012"
    )

    val mockAuthCacheService: AuthoritiesCacheService = mock[AuthoritiesCacheService]
    val mockAccCacheService: AccountsCacheService = mock[AccountsCacheService]

    val app: Application = applicationBuilder().build()

    val authorisedAccountsService: AuthorisedAccountsService = app.injector.instanceOf[AuthorisedAccountsService]

    implicit val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest()
    implicit val hc: HeaderCarrier = HeaderCarrier()

    def dataRequest(userAnswers: UserAnswers,
                    eori: EORI): DataRequest[AnyContentAsEmpty.type] =
      models.requests.DataRequest(
        fakeRequest(),
        InternalId("id"),
        Credentials(emptyString, emptyString),
        Organisation,
        Some(Name(Some("name"), Some("last"))), Some("email"),
        eori,
        userAnswers
      )
  }
}
