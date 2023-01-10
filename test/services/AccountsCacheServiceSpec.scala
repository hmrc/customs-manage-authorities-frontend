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
import connectors.CustomsFinancialsConnector
import models._
import models.domain._
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar._
import repositories.AccountsRepository
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.Future

class AccountsCacheServiceSpec extends SpecBase {

  "retrieveAccounts" must {
    "use cached values on cache hit" in new Setup {
      val service = new AccountsCacheService(mockRepository, mockConnector)(implicitly)
      val result = service.retrieveAccounts(InternalId("cachedId"), "GB098765432109")(hc)
      result.futureValue mustBe cachedAccounts
    }

    "update cache on cache miss" in new Setup {
      val service = new AccountsCacheService(mockRepository, mockConnector)(implicitly)
      val result = service.retrieveAccounts(InternalId("notCachedId"), "GB123456789012")(hc)
      result.futureValue mustBe notCachedAccounts
      verify(mockRepository, times(1)).set("notCachedId", notCachedAccounts)
    }

    "closed Account is valid" in new Setup {
      mockRepository = mock[AccountsRepository]
      when(mockRepository.get("cachedId")).thenReturn(Future.successful(Some(closedAccount)))
      val service = new AccountsCacheService(mockRepository, mockConnector)(implicitly)
      val result = service.retrieveAccounts(InternalId("cachedId"), "GB098765432109")(hc)
      result.futureValue mustBe closedAccount
    }

    "suspended Account is valid" in new Setup {
      mockRepository = mock[AccountsRepository]
      when(mockRepository.get("cachedId")).thenReturn(Future.successful(Some(suspendedAccount)))
      val service = new AccountsCacheService(mockRepository, mockConnector)(implicitly)
      val result = service.retrieveAccounts(InternalId("cachedId"), "GB098765432109")(hc)
      result.futureValue mustBe suspendedAccount
    }

    "pending Account is valid" in new Setup {
      mockRepository = mock[AccountsRepository]
      when(mockRepository.get("cachedId")).thenReturn(Future.successful(Some(pendingAccount)))
      val service = new AccountsCacheService(mockRepository, mockConnector)(implicitly)
      val result = service.retrieveAccounts(InternalId("cachedId"), "GB098765432109")(hc)
      result.futureValue mustBe pendingAccount
    }
  }
}

trait Setup {

  val notCachedAccounts = CDSAccounts("GB123456789012",
    List(CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))))

  val cachedAccounts = CDSAccounts("GB098765432109",
    List(CashAccount("54321", "GB098765432109", AccountStatusOpen, CDSCashBalance(Some(100.00)))))

  val closedAccount = CDSAccounts("GB098765432109",
    List(CashAccount("54321", "GB098765432109", AccountStatusClosed, CDSCashBalance(Some(100.00)))))

  val suspendedAccount = CDSAccounts("GB098765432109",
    List(CashAccount("54321", "GB098765432109", AccountStatusSuspended, CDSCashBalance(Some(100.00)))))

  val pendingAccount = CDSAccounts("GB098765432109",
    List(CashAccount("54321", "GB098765432109", AccountStatusPending, CDSCashBalance(Some(100.00)))))

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  var mockRepository = mock[AccountsRepository]
  when(mockRepository.get("cachedId")).thenReturn(Future.successful(Some(cachedAccounts)))
  when(mockRepository.get("notCachedId")).thenReturn(Future.successful(None))
  when(mockRepository.set(any(), any())).thenReturn(Future.successful(true))

  val mockConnector = mock[CustomsFinancialsConnector]
  when(mockConnector.retrieveAccounts(any())(any())).thenReturn(Future.successful(notCachedAccounts))
}