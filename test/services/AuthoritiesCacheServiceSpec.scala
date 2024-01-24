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
import models.InternalId
import models.domain.{
  AccountStatusOpen, AccountWithAuthorities, AccountWithAuthoritiesWithId, AuthoritiesWithId,
  CdsCashAccount, StandingAuthority
}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
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
      when(mockAuthRepo.get("notCachedId")).thenReturn(Future.successful(None))
      when(mockAuthRepo.set(any(), any())).thenReturn(Future.successful(true))

      when(mockConnector.retrieveAccountAuthorities(any())(any()))
        .thenReturn(Future.successful(Seq(accountWithAuthorities)))

      private val result = authCacheServices.retrieveAuthorities(InternalId("cachedId"), Seq(eoriNumber))(hc)

      result.futureValue mustBe cachedAuthorities
    }

    "update cache on cache miss" ignore new Setup {
      private val result =
        authCacheServices.retrieveAuthorities(InternalId("notCachedId"), Seq(eoriNumber))(hc).futureValue

      result.accounts.head.accountNumber mustEqual accountWithAuthorities.accountNumber

      verify(mockConnector, times(1)).retrieveAccountAuthorities(eoriNumber)
      verify(mockAuthRepo, times(1)).set("notCachedId", result)
    }
  }

  trait Setup {
    implicit lazy val hc: HeaderCarrier = HeaderCarrier()
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

    val eoriNumber = "GB123456789012"

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
      Map("a" ->
        AccountWithAuthoritiesWithId(CdsCashAccount, "12345", Some(AccountStatusOpen), Map("b" -> standingAuthority))
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
