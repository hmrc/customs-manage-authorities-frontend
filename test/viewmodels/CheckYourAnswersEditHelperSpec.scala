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

package viewmodels

import base.SpecBase
import models.{AuthorityStart, CompanyDetails, EoriDetailsCorrect, ShowBalance, UserAnswers}
import models.domain.{AccountStatusOpen, AccountWithAuthoritiesWithId, AuthorisedUser, CdsDutyDefermentAccount, DutyDefermentAccount, DutyDefermentBalance, StandingAuthority}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.add.{AccountsPage, AuthorityDetailsPage, AuthorityStartPage, EoriDetailsCorrectPage, EoriNumberPage, ShowBalancePage}
import play.api.i18n.Messages
import services.DateTimeService
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Value

import java.time.{LocalDate, LocalDateTime}

class CheckYourAnswersEditHelperSpec extends SpecBase with SummaryListRowHelper {

  "yourAccountRow" must {
    "produce correct rows" when {
      "EORI of Northern Ireland and account is of type Duty deferment" in {

        implicit val messages: Messages = messagesApi.preferred(fakeRequest())

        val mockDateTimeService: DateTimeService = mock[DateTimeService]
        when(mockDateTimeService.localTime()).thenReturn(LocalDateTime.now())

        val startDate = LocalDate.now()
        val standingAuthority = StandingAuthority("someEori", startDate, None, viewBalance = false)

        val accountsWithAuthoritiesWithId = AccountWithAuthoritiesWithId(
          CdsDutyDefermentAccount, "67890", Some(AccountStatusOpen), Map("b" -> standingAuthority))

        val standAuthority = StandingAuthority(
          "XI123456789012",
          LocalDate.now(),
          Option(LocalDate.now().plusDays(1)),
          viewBalance = true
        )

        val dutyDeferment: DutyDefermentAccount = DutyDefermentAccount(
          "67890",
          "XI210987654321",
          AccountStatusOpen,
          DutyDefermentBalance(None, None, None, None),
          isNiAccount = true)

        val cdsAccounts = List(dutyDeferment)

        val userAnswersWithNIEoriAndDefermentAccount: UserAnswers = UserAnswers("id")
          .set(AccountsPage, cdsAccounts).success.value
          .set(EoriNumberPage, CompanyDetails("XI123456789012", Some("companyName"))).success.value
          .set(AuthorityStartPage, AuthorityStart.Today)(AuthorityStart.writes).success.value
          .set(EoriDetailsCorrectPage, EoriDetailsCorrect.Yes)(EoriDetailsCorrect.writes).success.value
          .set(ShowBalancePage, ShowBalance.Yes)(ShowBalance.writes).success.value
          .set(AuthorityDetailsPage, AuthorisedUser("", "")).success.value

        val userAnswers = userAnswersWithNIEoriAndDefermentAccount.set(AccountsPage, List(dutyDeferment)).success.value
        val helper = new CheckYourAnswersEditHelper(userAnswers,
          "67890",
          "67890",
          mockDateTimeService,
          standAuthority,
          accountsWithAuthoritiesWithId,
          Some(""))

        helper.yourAccountRow.size mustBe 1
        helper.yourAccountRow.head.value mustBe
          Value(HtmlContent("manageAuthorities.table.heading.account.CdsDutyDefermentAccount manageAuthorities.table.heading.account.Northern-Ireland"))
      }
    }
  }
}
