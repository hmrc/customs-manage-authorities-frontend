/*
 * Copyright 2021 HM Revenue & Customs
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
import models.domain.{AccountStatusOpen, AccountWithAuthoritiesWithId, CdsCashAccount, StandingAuthority}
import models.{AuthorityEnd, AuthorityStart, ShowBalance}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.edit._
import play.api.test.Helpers._
import play.api.{Application, inject}
import repositories.SessionRepository

import java.time.LocalDate
import scala.concurrent.Future

class EditSessionServiceSpec extends SpecBase with MockitoSugar {
  "populateUserAnswers" should {
    "update the session with the user's answers from the api if no answers present in the session" in new Setup {
      val startDate = LocalDate.of(1, 1, 20)
      val standingAuthority = StandingAuthority("someEori", startDate, None, viewBalance = false)

      val accountsWithAuthoritiesWithId = AccountWithAuthoritiesWithId(CdsCashAccount, "12345", Some(AccountStatusOpen), Map("b" -> standingAuthority))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      running(app) {
        val result = await(service.resetUserAnswers("a", "b", emptyUserAnswers, standingAuthority, accountsWithAuthoritiesWithId)(messages(app)))
        val userAnswers = result.userAnswers

        userAnswers.get(EditAuthorityStartDatePage("a", "b")).get mustBe startDate
        userAnswers.get(EditAuthorityStartPage("a", "b")).get mustBe AuthorityStart.Setdate
        userAnswers.get(EditAuthorityEndDatePage("a", "b")) mustBe None
        userAnswers.get(EditAuthorityEndPage("a", "b")).get mustBe AuthorityEnd.Indefinite
      }
    }
  }


  trait Setup {
    val mockSessionRepository: SessionRepository = mock[SessionRepository]
    val mockDateTimeService: DateTimeService = mock[DateTimeService]

    val app: Application = applicationBuilder().overrides(
      inject.bind[SessionRepository].toInstance(mockSessionRepository),
      inject.bind[DateTimeService].toInstance(mockDateTimeService)
    ).build()

    val service: EditSessionService = app.injector.instanceOf[EditSessionService]
  }

}
