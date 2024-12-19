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

package services

import base.SpecBase
import connectors.CustomsFinancialsConnector
import models._
import models.domain._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar._
import play.api.{Application, inject}
import repositories.AccountsRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import utils.StringUtils.emptyString

class AccountsCacheServiceSpec extends SpecBase {

  "retrieveAccounts" must {
    "use cached values on cache hit" in new Setup {
      val service = new AccountsCacheService(mockRepository, mockConnector)(implicitly)

      val result: Future[CDSAccounts] = service.retrieveAccounts(InternalId("cachedId"), Seq("GB098765432109"))(hc)

      result.futureValue mustBe cachedAccounts
    }

    "closed Account is valid" in new Setup {
      when(mockRepository.get("cachedId")).thenReturn(Future.successful(Some(closedAccount)))
      val service = new AccountsCacheService(mockRepository, mockConnector)(implicitly)

      val result: Future[CDSAccounts] = service.retrieveAccounts(InternalId("cachedId"), Seq("GB098765432109"))(hc)

      result.futureValue mustBe closedAccount
    }

    "suspended Account is valid" in new Setup {
      when(mockRepository.get("cachedId")).thenReturn(Future.successful(Some(suspendedAccount)))
      val service = new AccountsCacheService(mockRepository, mockConnector)(implicitly)

      val result: Future[CDSAccounts] = service.retrieveAccounts(InternalId("cachedId"), Seq("GB098765432109"))(hc)

      result.futureValue mustBe suspendedAccount
    }

    "pending Account is valid" in new Setup {
      when(mockRepository.get("cachedId")).thenReturn(Future.successful(Some(pendingAccount)))
      val service = new AccountsCacheService(mockRepository, mockConnector)(implicitly)

      val result: Future[CDSAccounts] = service.retrieveAccounts(InternalId("cachedId"), Seq("GB098765432109"))(hc)

      result.futureValue mustBe pendingAccount
    }

    "merging account cache service must return flat account in merged list" in new Setup {
      val cashAccount: CashAccount =
        CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))

      val compare: CDSAccounts = CDSAccounts(emptyString, List(cashAccount))

      val service          = new AccountsCacheService(mockRepository, mockConnector)(implicitly)
      val res: CDSAccounts = service.merge(Seq(notCachedAccounts))

      res mustBe compare
    }
  }

  "retrieveAuthoritiesForId" must {

    "return return the authorities for the given InternalId" in new Setup {
      when(mockRepository.get("cachedId")).thenReturn(Future.successful(Some(cachedAccounts)))

      when(mockRepository.set(any(), any())).thenReturn(Future.successful(true))

      val result: Future[Option[CDSAccounts]] = accountCacheService.retrieveAccountsForId(InternalId("cachedId"))

      result.map { accounts =>
        accounts mustBe Some(cachedAccounts)
      }
    }
  }

  trait Setup {

    val notCachedAccounts: CDSAccounts = CDSAccounts(
      "GB123456789012",
      List(CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00))))
    )

    val cachedAccounts: CDSAccounts = CDSAccounts(
      "GB098765432109",
      List(CashAccount("54321", "GB098765432109", AccountStatusOpen, CDSCashBalance(Some(100.00))))
    )

    val closedAccount: CDSAccounts = CDSAccounts(
      "GB098765432109",
      List(CashAccount("54321", "GB098765432109", AccountStatusClosed, CDSCashBalance(Some(100.00))))
    )

    val suspendedAccount: CDSAccounts = CDSAccounts(
      "GB098765432109",
      List(CashAccount("54321", "GB098765432109", AccountStatusSuspended, CDSCashBalance(Some(100.00))))
    )

    val pendingAccount: CDSAccounts = CDSAccounts(
      "GB098765432109",
      List(CashAccount("54321", "GB098765432109", AccountStatusPending, CDSCashBalance(Some(100.00))))
    )

    implicit lazy val hc: HeaderCarrier                = HeaderCarrier()
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

    var mockRepository: AccountsRepository             = mock[AccountsRepository]
    val mockConnector: CustomsFinancialsConnector      = mock[CustomsFinancialsConnector]
    val mockAccountsCacheService: AccountsCacheService = mock[AccountsCacheService]

    when(mockRepository.get("cachedId")).thenReturn(Future.successful(Some(cachedAccounts)))
    when(mockRepository.get("notCachedId")).thenReturn(Future.successful(None))
    when(mockRepository.set(any(), any())).thenReturn(Future.successful(true))

    when(mockConnector.retrieveAccounts(any())(any())).thenReturn(Future.successful(notCachedAccounts))

    val app: Application = applicationBuilder()
      .overrides(
        inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
        inject.bind[AccountsRepository].toInstance(mockRepository)
      )
      .build()

    val accountCacheService: AccountsCacheService = app.injector.instanceOf[AccountsCacheService]
  }

}
