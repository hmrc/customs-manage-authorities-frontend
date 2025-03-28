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
import connectors.CustomsDataStoreConnector
import models.{AuthorityEnd, AuthorityStart}
import models.domain.{AccountStatusOpen, AccountWithAuthoritiesWithId, CdsCashAccount, StandingAuthority}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.edit._
import play.api.test.Helpers._
import play.api.{Application, inject}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.Future

class EditSessionServiceSpec extends SpecBase with MockitoSugar {
  "populateUserAnswers" should {

    "update the session with the user's answers for standingAuthority date set to today" in new Setup {
      val startDate         = LocalDate.now()
      val standingAuthority = StandingAuthority("someEori", startDate, None, viewBalance = false)

      val accountsWithAuthoritiesWithId = AccountWithAuthoritiesWithId(
        CdsCashAccount,
        "12345",
        Some(AccountStatusOpen),
        Map(authorityId -> standingAuthority)
      )

      running(app) {

        val result = await(
          service.resetUserAnswers(
            accountId,
            authorityId,
            emptyUserAnswers,
            standingAuthority,
            accountsWithAuthoritiesWithId,
            mockDataStoreConnector
          )(messages(app), hc)
        )

        val userAnswers = result.userAnswers

        userAnswers.get(EditAuthorityStartDatePage(accountId, authorityId)) mustBe None
        userAnswers.get(EditAuthorityStartPage(accountId, authorityId)).get mustBe AuthorityStart.Today
        userAnswers.get(EditAuthorityEndDatePage(accountId, authorityId)) mustBe None
        userAnswers.get(EditAuthorityEndPage(accountId, authorityId)).get mustBe AuthorityEnd.Indefinite
      }
    }

    "update the session with the user's answers from the api if no answers present in the session" in new Setup {
      val wrongYear         = 1
      val wrongMonth        = 1
      val wrongDayOfMonth   = 20
      val startDate         = LocalDate.of(wrongYear, wrongMonth, wrongDayOfMonth)
      val standingAuthority = StandingAuthority("someEori", startDate, None, viewBalance = false)

      val accountsWithAuthoritiesWithId = AccountWithAuthoritiesWithId(
        CdsCashAccount,
        "12345",
        Some(AccountStatusOpen),
        Map(authorityId -> standingAuthority)
      )

      running(app) {
        val result      = await(
          service.resetUserAnswers(
            accountId,
            authorityId,
            emptyUserAnswers,
            standingAuthority,
            accountsWithAuthoritiesWithId,
            mockDataStoreConnector
          )(messages(app), hc)
        )
        val userAnswers = result.userAnswers

        userAnswers.get(EditAuthorityStartDatePage(accountId, authorityId)).get mustBe startDate
        userAnswers.get(EditAuthorityStartPage(accountId, authorityId)).get mustBe AuthorityStart.Setdate
        userAnswers.get(EditAuthorityEndDatePage(accountId, authorityId)) mustBe None
        userAnswers.get(EditAuthorityEndPage(accountId, authorityId)).get mustBe AuthorityEnd.Indefinite
      }
    }
  }

  trait Setup {
    val mockSessionRepository: SessionRepository = mock[SessionRepository]
    val mockDateTimeService: DateTimeService     = mock[DateTimeService]
    val accountId                                = "123"
    val authorityId                              = "12345"

    implicit val hc: HeaderCarrier = HeaderCarrier()

    val app: Application = applicationBuilder()
      .overrides(
        inject.bind[SessionRepository].toInstance(mockSessionRepository),
        inject.bind[DateTimeService].toInstance(mockDateTimeService)
      )
      .build()

    val mockDataStoreConnector: CustomsDataStoreConnector = mock[CustomsDataStoreConnector]

    when(mockDataStoreConnector.retrieveCompanyInformationThirdParty(any())(any())).thenReturn(Future.successful(None))
    when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
    when(mockDateTimeService.localDate()).thenReturn(LocalDate.now())

    val service: EditSessionService = app.injector.instanceOf[EditSessionService]
  }
}
