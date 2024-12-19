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

package viewmodels

import java.time.LocalDateTime
import base.SpecBase
import models._
import models.domain._
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.add._
import play.api.i18n.Messages
import services.DateTimeService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Actions
import utils.StringUtils.emptyString

class EoriDetailsCorrectHelperSpec extends SpecBase with SummaryListRowHelper {

  implicit val messages: Messages = messagesApi.preferred(fakeRequest())

  val cashAccount: CashAccount = CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))

  val dutyDeferment: DutyDefermentAccount =
    DutyDefermentAccount("67890", "GB210987654321", AccountStatusOpen, DutyDefermentBalance(None, None, None, None))

  val generalGuarantee: GeneralGuaranteeAccount =
    GeneralGuaranteeAccount("54321", "GB000000000000", AccountStatusOpen, Some(GeneralGuaranteeBalance(50.00, 50.00)))

  val selectedAccounts: List[CDSAccount] = List(cashAccount, dutyDeferment, generalGuarantee)

  val userAnswersTodayToIndefinite: UserAnswers = UserAnswers("id")
    .set(AccountsPage, selectedAccounts)
    .success
    .value
    .set(EoriNumberPage, CompanyDetails("GB123456789012", Some("companyName")))
    .success
    .value
    .set(AuthorityStartPage, AuthorityStart.Today)(AuthorityStart.writes)
    .success
    .value
    .set(EoriDetailsCorrectPage, EoriDetailsCorrect.Yes)(EoriDetailsCorrect.writes)
    .success
    .value
    .set(ShowBalancePage, ShowBalance.Yes)(ShowBalance.writes)
    .success
    .value
    .set(AuthorityDetailsPage, AuthorisedUser(emptyString, emptyString))
    .success
    .value

  val userAnswersNoCompanyName: UserAnswers = UserAnswers("id")
    .set(AccountsPage, selectedAccounts)
    .success
    .value
    .set(EoriNumberPage, CompanyDetails("GB123456789012", None))
    .success
    .value
    .set(AuthorityStartPage, AuthorityStart.Today)(AuthorityStart.writes)
    .success
    .value
    .set(EoriDetailsCorrectPage, EoriDetailsCorrect.Yes)(EoriDetailsCorrect.writes)
    .success
    .value
    .set(ShowBalancePage, ShowBalance.Yes)(ShowBalance.writes)
    .success
    .value
    .set(AuthorityDetailsPage, AuthorisedUser(emptyString, emptyString))
    .success
    .value

  val mockDateTimeService: DateTimeService = mock[DateTimeService]

  when(mockDateTimeService.localTime()).thenReturn(LocalDateTime.now())

  "EoriDetailsCorrectHelper" must {

    "produce correct rows" when {

      "a EORI number and company name is displayed" in {
        val userAnswers = userAnswersTodayToIndefinite.set(AccountsPage, List(cashAccount)).success.value
        val helper      = EoriDetailsCorrectHelper(userAnswers, mockDateTimeService)
        helper.companyDetailsRows mustBe Seq(
          summaryListRow(
            "eoriDetail.eoriNumber.label",
            "GB123456789012",
            None,
            Actions(items = Seq())
          ),
          summaryListRow(
            "eoriDetail.companyName.label",
            "companyName",
            None,
            Actions(items = Seq())
          )
        )
      }

      "only EORI number row is displayed when no company name is present" in {
        val userAnswers = userAnswersNoCompanyName.set(AccountsPage, List(cashAccount)).success.value
        val helper      = EoriDetailsCorrectHelper(userAnswers, mockDateTimeService)
        helper.companyDetailsRows mustBe Seq(
          summaryListRow(
            "eoriDetail.eoriNumber.label",
            "GB123456789012",
            None,
            actions = Actions(items = Seq())
          )
        )
      }
    }
  }
}
