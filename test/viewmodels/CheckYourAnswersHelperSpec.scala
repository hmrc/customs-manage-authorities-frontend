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

import base.SpecBase
import models._
import models.domain._
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.add._
import services.DateTimeService
import uk.gov.hmrc.govukfrontend.views.Aliases.ActionItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Actions
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Value
import utils.StringUtils.emptyString
import viewmodels.ManageAuthoritiesViewModel.dateAsDayMonthAndYear

import java.time.{LocalDate, LocalDateTime}

class CheckYourAnswersHelperSpec extends SpecBase with SummaryListRowHelper {

  val cashAccount: CashAccount                  = CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))
  val dutyDeferment: DutyDefermentAccount       =
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
    .set(AuthorityEndPage, AuthorityEnd.Indefinite)(AuthorityEnd.writes)
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

  "CheckYourAnswersHelper" must {
    "produce correct rows" when {

      "Plural specific title is set when more than one cashAccount is passed" in {
        val cashAccount02: CashAccount =
          CashAccount("12346", "GB123456789013", AccountStatusOpen, CDSCashBalance(Some(101.00)))
        val userAnswers                = userAnswersNoCompanyName.set(AccountsPage, List(cashAccount, cashAccount02)).success.value
        val helper                     = CheckYourAnswersHelper(userAnswers, mockDateTimeService)

        helper.accountsTitle mustBe messages("checkYourAnswers.accounts.h2.plural")
        helper.accountsRows.size mustBe 1
      }

      "summaryListRowHelper should give correct values for yesOrNo" in {
        yesOrNo(true) mustEqual "site.yes"
        yesOrNo(false) mustEqual "site.no"
      }

      "accountNumberRow should return valid message if xiEori is true" in {
        val cdsAccount: AccountWithAuthoritiesWithId =
          AccountWithAuthoritiesWithId(
            CdsDutyDefermentAccount,
            "12345",
            Some(AccountStatusOpen),
            Map(
              "b" -> StandingAuthority(
                "EORI",
                LocalDate.now().plusMonths(1),
                Some(LocalDate.parse("2020-04-01")),
                viewBalance = false
              )
            )
          )

        accountNumberRow(cdsAccount, true).get mustBe a[SummaryListRow]
      }

      "only EORI number row is displayed when no company name is present" in {
        val userAnswers = userAnswersNoCompanyName.set(AccountsPage, List(cashAccount)).success.value
        val helper      = CheckYourAnswersHelper(userAnswers, mockDateTimeService)
        helper.accountsRows mustBe Seq(
          summaryListRow(
            "accounts.checkYourAnswersLabel.singular",
            "accounts.type.cash: 12345",
            None,
            Actions(items =
              Seq(
                ActionItem(
                  href = controllers.add.routes.AccountsController.onPageLoad(CheckMode).url,
                  content = span("site.change"),
                  visuallyHiddenText = Some("checkYourAnswers.accounts.hidden")
                )
              )
            )
          )
        )
        helper.companyDetailsRows mustBe Seq(
          summaryListRow(
            "checkYourAnswers.eoriNumber.label",
            "GB123456789012",
            None,
            actions = Actions(items =
              Seq(
                ActionItem(
                  href = controllers.add.routes.EoriNumberController.onPageLoad(CheckMode).url,
                  content = span(messages("site.change")),
                  visuallyHiddenText = Some(messages("checkYourAnswers.eoriNumber.hidden"))
                )
              )
            )
          ),
          summaryListRow("view-authority-h2.5", "view-authority-h2.6", None, actions = Actions(items = Seq()))
        )

        helper.authorityDurationRows mustBe Seq(
          summaryListRow(
            "authorityStart.checkYourAnswersLabel",
            s"authorityStart.checkYourAnswersLabel.today ${dateAsDayMonthAndYear(LocalDate.now())}",
            None,
            actions = Actions(items =
              Seq(
                ActionItem(
                  href = controllers.add.routes.AuthorityStartController.onPageLoad(CheckMode).url,
                  content = span(messages("site.change")),
                  visuallyHiddenText = Some(messages("checkYourAnswers.authorityStart.hidden"))
                )
              )
            )
          ),
          summaryListRow(
            "authorityEnd.checkYourAnswersLabel",
            s"checkYourAnswers.authorityEnd.indefinite",
            None,
            actions = Actions(items =
              Seq(
                ActionItem(
                  href = controllers.add.routes.AuthorityEndController.onPageLoad(CheckMode).url,
                  content = span(messages("site.change")),
                  visuallyHiddenText = Some(messages("checkYourAnswers.authorityEnd.hidden"))
                )
              )
            )
          ),
          summaryListRow(
            "showBalance.checkYourAnswersLabel",
            "showBalance.checkYourAnswers.yes",
            None,
            actions = Actions(items =
              Seq(
                ActionItem(
                  href = controllers.add.routes.ShowBalanceController.onPageLoad(CheckMode).url,
                  content = span(messages("site.change")),
                  visuallyHiddenText = Some(messages("checkYourAnswers.showBalance.hidden"))
                )
              )
            )
          )
        )
      }
    }

    "produce correct authorisation declaration message" when {

      "a single account is selected" in {
        val userAnswers: UserAnswers       = userAnswersTodayToIndefinite.set(AccountsPage, List(cashAccount)).success.value
        val helper: CheckYourAnswersHelper = CheckYourAnswersHelper(userAnswers, mockDateTimeService)
        helper.accountsTitle mustBe "checkYourAnswers.accounts.h2.singular"
      }

      "multiple accounts are selected" in {
        val helper: CheckYourAnswersHelper = CheckYourAnswersHelper(userAnswersTodayToIndefinite, mockDateTimeService)
        helper.accountsTitle mustBe "checkYourAnswers.accounts.h2.plural"
      }
    }

    "produce correct text for EORI of Northern Ireland" when {
      "account is of type Duty deferment" in {

        val dutyDeferment: DutyDefermentAccount = DutyDefermentAccount(
          "67890",
          "GB210987654321",
          AccountStatusOpen,
          DutyDefermentBalance(None, None, None, None),
          isNiAccount = true
        )

        val cdsAccounts = List(dutyDeferment)

        val userAnswersWithNIEoriAndDefermentAccount: UserAnswers = UserAnswers("id")
          .set(AccountsPage, cdsAccounts)
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

        val userAnswers = userAnswersWithNIEoriAndDefermentAccount.set(AccountsPage, List(dutyDeferment)).success.value
        val helper      = CheckYourAnswersHelper(userAnswers, mockDateTimeService)

        helper.accountsRows.size mustBe 1
        helper.accountsRows.head.value mustBe Value(HtmlContent("accounts.type.dutyDeferment accounts.ni: 67890"))
      }
    }

    "produce correct text for EORI" when {
      "account is of type Duty deferment" in {

        val dutyDeferment: DutyDefermentAccount = DutyDefermentAccount(
          "67890",
          "GB210987654321",
          AccountStatusOpen,
          DutyDefermentBalance(None, None, None, None)
        )

        val cdsAccounts = List(dutyDeferment)

        val userAnswersWithNIEoriAndDefermentAccount: UserAnswers = UserAnswers("id")
          .set(AccountsPage, cdsAccounts)
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

        val userAnswers = userAnswersWithNIEoriAndDefermentAccount.set(AccountsPage, List(dutyDeferment)).success.value
        val helper      = CheckYourAnswersHelper(userAnswers, mockDateTimeService)

        helper.accountsRows.size mustBe 1
        helper.accountsRows.head.value mustBe Value(HtmlContent("accounts.type.dutyDeferment: 67890"))
      }
    }
  }
}
