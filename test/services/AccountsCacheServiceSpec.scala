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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar._
import play.api.Application
import repositories.AccountsRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import utils.StringUtils.emptyString

class AccountsCacheServiceSpec extends SpecBase {

  "retrieveAccounts" must {
    "use cached values on cache hit" in new Setup {
      val service = new AccountsCacheService(mockRepository, mockConnector)(implicitly)

      val result = service.retrieveAccounts(InternalId("cachedId"), Seq("GB098765432109"))(hc)

      result.futureValue mustBe cachedAccounts
    }

    "closed Account is valid" in new Setup {
      when(mockRepository.get("cachedId")).thenReturn(Future.successful(Some(closedAccount)))
      val service = new AccountsCacheService(mockRepository, mockConnector)(implicitly)

      val result = service.retrieveAccounts(InternalId("cachedId"), Seq("GB098765432109"))(hc)

      result.futureValue mustBe closedAccount
    }

    "suspended Account is valid" in new Setup {
      when(mockRepository.get("cachedId")).thenReturn(Future.successful(Some(suspendedAccount)))
      val service = new AccountsCacheService(mockRepository, mockConnector)(implicitly)

      val result = service.retrieveAccounts(InternalId("cachedId"), Seq("GB098765432109"))(hc)

      result.futureValue mustBe suspendedAccount
    }

    "pending Account is valid" in new Setup {
      when(mockRepository.get("cachedId")).thenReturn(Future.successful(Some(pendingAccount)))
      val service = new AccountsCacheService(mockRepository, mockConnector)(implicitly)

      val result = service.retrieveAccounts(InternalId("cachedId"), Seq("GB098765432109"))(hc)

      result.futureValue mustBe pendingAccount
    }

    "merging account cache service must return flat account in merged list" in new Setup {
      val cashAccount = CashAccount(
        "12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))

      val compare = CDSAccounts(emptyString,List(cashAccount))

      val service = new AccountsCacheService(mockRepository, mockConnector)(implicitly)
      val res = service.merge(Seq(notCachedAccounts))

      res mustBe compare
    }
  }

  "retrieveAuthoritiesForId" must {

    "return return the authorities for the given InternalId" in new Setup {
      when(mockRepository.get("cachedId")).thenReturn(Future.successful(Some(cachedAuthorities)))

      when(mockRepository.set(any(), any())).thenReturn(Future.successful(true))

      val result: Future[Option[AuthoritiesWithId]] = authCacheServices.retrieveAuthoritiesForId(InternalId("cachedId"))

      result.map {
        authorities => authorities mustBe Some(InternalId("cachedId"))
      }
    }
  }

  trait Setup {

    val notCachedAccounts: CDSAccounts = CDSAccounts("GB123456789012",
      List(CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))))

    val cachedAccounts: CDSAccounts = CDSAccounts("GB098765432109",
      List(CashAccount("54321", "GB098765432109", AccountStatusOpen, CDSCashBalance(Some(100.00)))))

    val closedAccount: CDSAccounts = CDSAccounts("GB098765432109",
      List(CashAccount("54321", "GB098765432109", AccountStatusClosed, CDSCashBalance(Some(100.00)))))

    val suspendedAccount: CDSAccounts = CDSAccounts("GB098765432109",
      List(CashAccount("54321", "GB098765432109", AccountStatusSuspended, CDSCashBalance(Some(100.00)))))

    val pendingAccount: CDSAccounts = CDSAccounts("GB098765432109",
      List(CashAccount("54321", "GB098765432109", AccountStatusPending, CDSCashBalance(Some(100.00)))))

    implicit lazy val hc: HeaderCarrier = HeaderCarrier()
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

    var mockRepository: AccountsRepository = mock[AccountsRepository]

    when(mockRepository.get("cachedId")).thenReturn(Future.successful(Some(cachedAccounts)))
    when(mockRepository.get("notCachedId")).thenReturn(Future.successful(None))
    when(mockRepository.set(any(), any())).thenReturn(Future.successful(true))

    val mockConnector: CustomsFinancialsConnector = mock[CustomsFinancialsConnector]

    when(mockConnector.retrieveAccounts(any())(any())).thenReturn(Future.successful(notCachedAccounts))

    val mockConnector: CustomsFinancialsConnector = mock[CustomsFinancialsConnector]
    val mockAccountsCacheService: AccountsCacheService = mock[AccountsCacheService]

 /*   val app: Application = applicationBuilder().overrides(
      inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
      inject.bind[AuthoritiesRepository].toInstance(mockAuthRepo)
    ).build()*/

    val authCacheServices: AuthoritiesCacheService = app.injector.instanceOf[AuthoritiesCacheService]
  }

}
