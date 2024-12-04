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
import models.InternalId
import models.domain.{AccountStatusOpen, AccountWithAuthorities, AccountWithAuthoritiesWithId, AuthoritiesWithId, CdsCashAccount, StandingAuthority}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.{Application, inject}
import repositories.AuthoritiesRepository
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.Future

class AuthoritiesCacheServiceSpec extends SpecBase {

  "retrieveAuthorities" must {

    "use cached values on cache hit" in new Setup {
      when(mockAuthRepo.get("cachedId")).thenReturn(Future.successful(Some(cachedAuthorities)))

      when(mockAuthRepo.set(any(), any())).thenReturn(Future.successful(true))

      when(mockConnector.retrieveAccountAuthorities(any())(any()))
        .thenReturn(Future.successful(Seq(accountWithAuthorities)))

      private val result = authCacheServices.retrieveAuthorities(InternalId("cachedId"), Seq(eoriNumber))(hc)

      result.futureValue mustBe cachedAuthorities
    }

    "update cache on cache miss" in new Setup {
      when(mockAuthRepo.get("notCachedId")).thenReturn(Future.successful(None))

      when(mockConnector.retrieveAccountAuthorities(any())(any()))
        .thenReturn(Future.successful(Seq(accountWithAuthorities)))

      when(mockAuthRepo.set(any(), any())).thenReturn(Future.successful(true))

      private val result: AuthoritiesWithId =
        authCacheServices.retrieveAuthorities(InternalId("notCachedId"), Seq(eoriNumber))(hc).futureValue

      result.accounts.head.accountNumber mustEqual accountWithAuthorities.accountNumber

      verify(mockConnector, times(1)).retrieveAccountAuthorities(eoriNumber)
      verify(mockAuthRepo, times(1)).set("notCachedId", result)
    }
  }

  "getAccountAndAuthority" must {

    "return correct AccountAndAuthority when there is no error" in new Setup {
      when(mockAuthRepo.get("cachedId")).thenReturn(Future.successful(Some(cachedAuthorities)))

      when(mockAuthRepo.set(any(), any())).thenReturn(Future.successful(true))

      private val result: Either[AuthoritiesCacheErrorResponse, AccountAndAuthority] =
        authCacheServices.getAccountAndAuthority(
          InternalId("cachedId"),
          authorityId = authorityIdB,
          accountId = accountIdA)(hc).futureValue

      result shouldBe Right(AccountAndAuthority(
        AccountWithAuthoritiesWithId(
          CdsCashAccount, "12345",
          Some(AccountStatusOpen),
          Map(authorityIdB -> standingAuthority)),
        standingAuthority))

      verify(mockConnector, times(0)).retrieveAccountAuthorities(eoriNumber)
    }

    "return NoAccount when authority is not found for the accountId" in new Setup {
      when(mockAuthRepo.get("cachedId")).thenReturn(Future.successful(Some(cachedAuthorities)))

      when(mockAuthRepo.set(any(), any())).thenReturn(Future.successful(true))

      private val result: Either[AuthoritiesCacheErrorResponse, AccountAndAuthority] =
        authCacheServices.getAccountAndAuthority(
          InternalId("cachedId"),
          authorityId = authorityIdB,
          accountId = accountIdUnknown)(hc).futureValue

      result shouldBe Left(NoAccount)

      verify(mockConnector, times(0)).retrieveAccountAuthorities(eoriNumber)
    }
  }

  "retrieveAuthoritiesForId" must {

    "return return the authorities for the given InternalId" in new Setup {
      when(mockAuthRepo.get("cachedId")).thenReturn(Future.successful(Some(cachedAuthorities)))

      when(mockAuthRepo.set(any(), any())).thenReturn(Future.successful(true))

      val result: Future[Option[AuthoritiesWithId]] = authCacheServices.retrieveAuthoritiesForId(InternalId("cachedId"))

      result.map {
        authorities => authorities mustBe Some(InternalId("cachedId"))
      }
    }
  }

  trait Setup {
    implicit lazy val hc: HeaderCarrier = HeaderCarrier()
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

    val eoriNumber = "GB123456789012"
    val authorityIdB = "b"
    val accountIdA = "a"
    val accountIdUnknown = "unknown"

    val dateString1 = "2020-03-01"
    val dateString2 = "2020-04-01"
    val startDate: LocalDate = LocalDate.parse(dateString1)
    val endDate: LocalDate = LocalDate.parse(dateString2)

    val standingAuthority: StandingAuthority = StandingAuthority(
      "EORI",
      LocalDate.parse(dateString1),
      Some(LocalDate.parse(dateString2)),
      viewBalance = false
    )

    val accountWithAuthorities: AccountWithAuthorities =
      AccountWithAuthorities(CdsCashAccount, "54321", Some(AccountStatusOpen), Seq(standingAuthority))

    val cachedAuthorities: AuthoritiesWithId = AuthoritiesWithId(
      Map(accountIdA ->
        AccountWithAuthoritiesWithId(
          CdsCashAccount,
          "12345",
          Some(AccountStatusOpen),
          Map(authorityIdB -> standingAuthority))
      ))

    val mockConnector: CustomsFinancialsConnector = mock[CustomsFinancialsConnector]
    val mockAuthRepo: AuthoritiesRepository = mock[AuthoritiesRepository]

    val app: Application = applicationBuilder().overrides(
      inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
      inject.bind[AuthoritiesRepository].toInstance(mockAuthRepo)
    ).build()

    val authCacheServices: AuthoritiesCacheService = app.injector.instanceOf[AuthoritiesCacheService]
  }

}
