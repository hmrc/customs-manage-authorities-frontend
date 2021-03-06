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
import repositories.AuthoritiesRepository
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.Future

class AuthoritiesCacheServiceSpec extends SpecBase {

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val startDate = LocalDate.parse("2020-03-01")
  val endDate = LocalDate.parse("2020-04-01")
  val standingAuthority = StandingAuthority(
    "EORI",
    LocalDate.parse("2020-03-01"),
    viewBalance = false
  )

  val accountWithAuthorities = AccountWithAuthorities(CdsCashAccount, "54321", Some(AccountStatusOpen), Seq(standingAuthority))
  val cachedAuthorities: AuthoritiesWithId = AuthoritiesWithId(Map(
    ("a" -> AccountWithAuthoritiesWithId(CdsCashAccount, "12345", Some(AccountStatusOpen), Map("b" -> standingAuthority)))
  ))

  val mockRepository = mock[AuthoritiesRepository]
  when(mockRepository.get("cachedId")).thenReturn(Future.successful(Some(cachedAuthorities)))
  when(mockRepository.get("notCachedId")).thenReturn(Future.successful(None))
  when(mockRepository.set(any(), any())).thenReturn(Future.successful(true))

  val mockConnector = mock[CustomsFinancialsConnector]
  when(mockConnector.retrieveAccountAuthorities()(any())).thenReturn(Future.successful(Seq(accountWithAuthorities)))

  "retrieveAuthorities" must {

    "use cached values on cache hit" in {
      val service = new AuthoritiesCacheService(mockRepository, mockConnector)(implicitly)
      val result = service.retrieveAuthorities(InternalId("cachedId"))(hc)

      result.futureValue mustBe cachedAuthorities
    }

    "update cache on cache miss" in {
      val service = new AuthoritiesCacheService(mockRepository, mockConnector)(implicitly)
      val result = service.retrieveAuthorities(InternalId("notCachedId"))(hc).futureValue

      result.accounts.head.accountNumber mustEqual accountWithAuthorities.accountNumber

      verify(mockConnector, times(1)).retrieveAccountAuthorities()
      verify(mockRepository, times(1)).set("notCachedId", result)
    }

  }

}
