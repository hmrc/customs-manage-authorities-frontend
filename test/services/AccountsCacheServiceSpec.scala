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

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val notCachedAccounts = CDSAccounts(
    "GB123456789012",
    List(CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00))))
  )
  val cachedAccounts = CDSAccounts(
    "GB098765432109",
    List(CashAccount("54321", "GB098765432109", AccountStatusOpen, CDSCashBalance(Some(100.00))))
  )

  val mockRepository = mock[AccountsRepository]
  when(mockRepository.get("cachedId")).thenReturn(Future.successful(Some(cachedAccounts)))
  when(mockRepository.get("notCachedId")).thenReturn(Future.successful(None))
  when(mockRepository.set(any(), any())).thenReturn(Future.successful(true))

  val mockConnector = mock[CustomsFinancialsConnector]
  when(mockConnector.retrieveAccounts(any())(any())).thenReturn(Future.successful(notCachedAccounts))

  "retrieveAccounts" must {

    "use cached values on cache hit" in {
      val service = new AccountsCacheService(mockRepository, mockConnector)(implicitly)
      val result = service.retrieveAccounts(InternalId("cachedId"), "GB098765432109")(hc)

      result.futureValue mustBe cachedAccounts
    }

    "update cache on cache miss" in {
      val service = new AccountsCacheService(mockRepository, mockConnector)(implicitly)
      val result = service.retrieveAccounts(InternalId("notCachedId"), "GB123456789012")(hc)

      result.futureValue mustBe notCachedAccounts
      verify(mockRepository, times(1)).set("notCachedId", notCachedAccounts)
    }

  }

}
