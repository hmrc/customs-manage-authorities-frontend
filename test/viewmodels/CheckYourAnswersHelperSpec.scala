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

package viewmodels

import base.SpecBase
import models._
import models.domain._
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.add._
import play.api.i18n.Messages
import play.api.libs.json.{JsString, Writes}
import services.DateTimeService
import uk.gov.hmrc.govukfrontend.views.Aliases.ActionItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Actions
import viewmodels.ManageAuthoritiesViewModel.dateAsDayMonthAndYear

import java.time.{LocalDate, LocalDateTime}

class CheckYourAnswersHelperSpec extends SpecBase with SummaryListRowHelper {

  implicit val messages: Messages = messagesApi.preferred(fakeRequest())

  val cashAccount: CashAccount = CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))
  val dutyDeferment: DutyDefermentAccount = DutyDefermentAccount("67890", "GB210987654321", AccountStatusOpen, DutyDefermentBalance(None, None, None, None))
  val generalGuarantee: GeneralGuaranteeAccount = GeneralGuaranteeAccount("54321", "GB000000000000", AccountStatusOpen, Some(GeneralGuaranteeBalance(50.00, 50.00)))

  val selectedAccounts: List[CDSAccount] = List(cashAccount, dutyDeferment, generalGuarantee)

  val userAnswersTodayToIndefinite: UserAnswers = UserAnswers("id")
    .set(AccountsPage, selectedAccounts).success.value
    .set(EoriNumberPage, CompanyDetails("GB123456789012", Some("companyName"))).success.value
    .set(AuthorityStartPage, AuthorityStart.Today)(AuthorityStart.writes).success.value
    .set(EoriDetailsCorrectPage, EoriDetailsCorrect.Yes)(EoriDetailsCorrect.writes).success.value
    .set(ShowBalancePage, ShowBalance.Yes)(ShowBalance.writes).success.value
    .set(AuthorityDetailsPage, AuthorisedUser("", "")).success.value

  val userAnswersNoCompanyName: UserAnswers = UserAnswers("id")
    .set(AccountsPage, selectedAccounts).success.value
    .set(EoriNumberPage, CompanyDetails("GB123456789012", None)).success.value
    .set(AuthorityStartPage, AuthorityStart.Today)(AuthorityStart.writes).success.value
    .set(EoriDetailsCorrectPage, EoriDetailsCorrect.Yes)(EoriDetailsCorrect.writes).success.value
    .set(ShowBalancePage, ShowBalance.Yes)(ShowBalance.writes).success.value
    .set(AuthorityDetailsPage, AuthorisedUser("", "")).success.value


  val mockDateTimeService: DateTimeService = mock[DateTimeService]
  when(mockDateTimeService.localTime()).thenReturn(LocalDateTime.now())

  "CheckYourAnswersHelper" must {

    "produce correct rows" when {

      "a single account is selected (today to indefinite)" in {
        val userAnswers = userAnswersTodayToIndefinite.set(AccountsPage, List(cashAccount)).success.value
        val helper = CheckYourAnswersHelper(userAnswers, mockDateTimeService)
        helper.accountsRows mustBe Seq(
          summaryListRow(
            "accounts.checkYourAnswersLabel.singular",
            "accounts.type.cash: 12345",
            None,
            Actions(items = Seq(ActionItem(
              href = controllers.add.routes.AccountsController.onPageLoad(CheckMode).url,
              content = span("site.change"),
              visuallyHiddenText = Some("checkYourAnswers.accounts.hidden")
            )))
          ))
        helper.companyDetailsRows mustBe Seq(
          summaryListRow(
            "checkYourAnswers.eoriNumber.label",
            "GB123456789012",
            None,
            actions = Actions(items = Seq(ActionItem(
              href = controllers.add.routes.EoriNumberController.onPageLoad(CheckMode).url,
              content = span(messages("site.change")),
              visuallyHiddenText = Some(messages("checkYourAnswers.eoriNumber.hidden"))
            )))
          ),
          summaryListRow(
            "checkYourAnswers.companyName.label",
            "companyName",
            None,
            actions = Actions(items = Seq()
          )))
        helper.authorityDurationRows mustBe Seq(
          summaryListRow(
            "authorityStart.checkYourAnswersLabel",
            s"authorityStart.checkYourAnswersLabel.today ${dateAsDayMonthAndYear(LocalDate.now())}",
            None,
            actions = Actions(items = Seq(ActionItem(
              href = controllers.add.routes.AuthorityStartController.onPageLoad(CheckMode).url,
              content = span(messages("site.change")),
              visuallyHiddenText = Some(messages("checkYourAnswers.authorityStart.hidden"))
            )))
          ),
          summaryListRow(
            "showBalance.checkYourAnswersLabel",
            "showBalance.checkYourAnswers.yes",
            None,
            actions = Actions(items = Seq(ActionItem(
              href = controllers.add.routes.ShowBalanceController.onPageLoad(CheckMode).url,
              content = span(messages("site.change")),
              visuallyHiddenText = Some(messages("checkYourAnswers.showBalance.hidden"))
            )))
          )
        )
      }

      "only EORI number row is displayed when no company name is present" in {
        val userAnswers = userAnswersNoCompanyName.set(AccountsPage, List(cashAccount)).success.value
        val helper = CheckYourAnswersHelper(userAnswers, mockDateTimeService)
        helper.accountsRows mustBe Seq(
          summaryListRow(
            "accounts.checkYourAnswersLabel.singular",
            "accounts.type.cash: 12345",
            None,
            Actions(items = Seq(ActionItem(
              href = controllers.add.routes.AccountsController.onPageLoad(CheckMode).url,
              content = span("site.change"),
              visuallyHiddenText = Some("checkYourAnswers.accounts.hidden")
            )))
          ))
        helper.companyDetailsRows mustBe Seq(
          summaryListRow(
            "checkYourAnswers.eoriNumber.label",
            "GB123456789012",
            None,
            actions = Actions(items = Seq(ActionItem(
              href = controllers.add.routes.EoriNumberController.onPageLoad(CheckMode).url,
              content = span(messages("site.change")),
              visuallyHiddenText = Some(messages("checkYourAnswers.eoriNumber.hidden"))
            )))
          ))
        helper.authorityDurationRows mustBe Seq(
          summaryListRow(
            "authorityStart.checkYourAnswersLabel",
            s"authorityStart.checkYourAnswersLabel.today ${dateAsDayMonthAndYear(LocalDate.now())}",
            None,
            actions = Actions(items = Seq(ActionItem(
              href = controllers.add.routes.AuthorityStartController.onPageLoad(CheckMode).url,
              content = span(messages("site.change")),
              visuallyHiddenText = Some(messages("checkYourAnswers.authorityStart.hidden"))
            )))
          ),
          summaryListRow(
            "showBalance.checkYourAnswersLabel",
            "showBalance.checkYourAnswers.yes",
            None,
            actions = Actions(items = Seq(ActionItem(
              href = controllers.add.routes.ShowBalanceController.onPageLoad(CheckMode).url,
              content = span(messages("site.change")),
              visuallyHiddenText = Some(messages("checkYourAnswers.showBalance.hidden"))
            )))
          )
        )
      }

      "a single account is selected (set date to set date)" in {
        implicit val writes: Writes[LocalDate] = (o: LocalDate) => JsString(o.toString)

        val userAnswers = userAnswersTodayToIndefinite
          .set(AccountsPage, List(cashAccount)).success.value
          .set(AuthorityStartPage, AuthorityStart.Setdate)(AuthorityStart.writes).success.value
          .set(AuthorityStartDatePage, LocalDate.now().plusDays(1)).success.value
        val helper = CheckYourAnswersHelper(userAnswers, mockDateTimeService)
        helper.accountsRows mustBe Seq(
          summaryListRow(
            "accounts.checkYourAnswersLabel.singular",
            "accounts.type.cash: 12345",
            None,
            Actions(items = Seq(ActionItem(
              href = controllers.add.routes.AccountsController.onPageLoad(CheckMode).url,
              content = span("site.change"),
              visuallyHiddenText = Some("checkYourAnswers.accounts.hidden")
            )))
          ))
        helper.companyDetailsRows mustBe Seq(
          summaryListRow(
            "checkYourAnswers.eoriNumber.label",
            "GB123456789012",
            None,
            actions = Actions(items = Seq(ActionItem(
              href = controllers.add.routes.EoriNumberController.onPageLoad(CheckMode).url,
              content = span(messages("site.change")),
              visuallyHiddenText = Some(messages("checkYourAnswers.eoriNumber.hidden"))
            )))
          ),
          summaryListRow(
            "checkYourAnswers.companyName.label",
            "companyName",
            None,
            actions = Actions(items = Seq())
          ))

        helper.authorityDurationRows mustBe Seq(
          summaryListRow(
            "authorityStart.checkYourAnswersLabel",
            dateAsDayMonthAndYear(LocalDate.now().plusDays(1)),
            None,
            actions = Actions(items = Seq(ActionItem(
              href = controllers.add.routes.AuthorityStartController.onPageLoad(CheckMode).url,
              content = span(messages("site.change")),
              visuallyHiddenText = Some(messages("checkYourAnswers.authorityStart.hidden"))
            )))
          ),
          summaryListRow(
            "showBalance.checkYourAnswersLabel",
            "showBalance.checkYourAnswers.yes",
            None,
            actions = Actions(items = Seq(ActionItem(
              href = controllers.add.routes.ShowBalanceController.onPageLoad(CheckMode).url,
              content = span(messages("site.change")),
              visuallyHiddenText = Some(messages("checkYourAnswers.showBalance.hidden"))
            )))
          )
        )
      }

      "multiple accounts are selected (today to indefinite)" in {
        val userAnswers = userAnswersTodayToIndefinite.set(ShowBalancePage, ShowBalance.No).success.value
        val helper = CheckYourAnswersHelper(userAnswers, mockDateTimeService)
        helper.accountsRows mustBe Seq(
          summaryListRow(
            "accounts.checkYourAnswersLabel.plural",
            "accounts.type.cash: 12345<br>accounts.type.dutyDeferment: 67890<br>accounts.type.generalGuarantee: 54321",
            None,
            Actions(items = Seq(ActionItem(
              href = controllers.add.routes.AccountsController.onPageLoad(CheckMode).url,
              content = span("site.change"),
              visuallyHiddenText = Some("checkYourAnswers.accounts.hidden")
            )))
          ))
        helper.companyDetailsRows mustBe Seq(
          summaryListRow(
            "checkYourAnswers.eoriNumber.label",
            "GB123456789012",
            None,
            actions = Actions(items = Seq(ActionItem(
              href = controllers.add.routes.EoriNumberController.onPageLoad(CheckMode).url,
              content = span(messages("site.change")),
              visuallyHiddenText = Some(messages("checkYourAnswers.eoriNumber.hidden"))
            )))
          ),
          summaryListRow(
            "checkYourAnswers.companyName.label",
            "companyName",
            None,
            actions = Actions(items = Seq())
          ))
        helper.authorityDurationRows mustBe Seq(
          summaryListRow(
            "authorityStart.checkYourAnswersLabel",
            s"authorityStart.checkYourAnswersLabel.today ${dateAsDayMonthAndYear(LocalDate.now())}",
            None,
            actions = Actions(items = Seq(ActionItem(
              href = controllers.add.routes.AuthorityStartController.onPageLoad(CheckMode).url,
              content = span(messages("site.change")),
              visuallyHiddenText = Some(messages("checkYourAnswers.authorityStart.hidden"))
            )))
          ),
          summaryListRow(
            "showBalance.checkYourAnswersLabel",
            "showBalance.checkYourAnswers.no",
            None,
            actions = Actions(items = Seq(ActionItem(
              href = controllers.add.routes.ShowBalanceController.onPageLoad(CheckMode).url,
              content = span(messages("site.change")),
              visuallyHiddenText = Some(messages("checkYourAnswers.showBalance.hidden"))
            )))
          )
        )
      }

      "multiple accounts are selected (set date to set date)" in {
        implicit val writes: Writes[LocalDate] = (o: LocalDate) => JsString(o.toString)

        val userAnswers = userAnswersTodayToIndefinite
          .set(AuthorityStartPage, AuthorityStart.Setdate).success.value
          .set(AuthorityStartDatePage, LocalDate.now().plusDays(1)).success.value
        val helper = CheckYourAnswersHelper(userAnswers, mockDateTimeService)
        helper.accountsRows mustBe Seq(
          summaryListRow(
            "accounts.checkYourAnswersLabel.plural",
            "accounts.type.cash: 12345<br>accounts.type.dutyDeferment: 67890<br>accounts.type.generalGuarantee: 54321",
            None,
            Actions(items = Seq(ActionItem(
              href = controllers.add.routes.AccountsController.onPageLoad(CheckMode).url,
              content = span("site.change"),
              visuallyHiddenText = Some("checkYourAnswers.accounts.hidden")
            )))
          ))
        helper.companyDetailsRows mustBe Seq(
          summaryListRow(
            "checkYourAnswers.eoriNumber.label",
            "GB123456789012",
            None,
            actions = Actions(items = Seq(ActionItem(
              href = controllers.add.routes.EoriNumberController.onPageLoad(CheckMode).url,
              content = span(messages("site.change")),
              visuallyHiddenText = Some(messages("checkYourAnswers.eoriNumber.hidden"))
            )))
          ),
          summaryListRow(
            "checkYourAnswers.companyName.label",
            "companyName",
            None,
            actions = Actions(items = Seq())
          ))
        helper.authorityDurationRows mustBe Seq(
          summaryListRow(
            "authorityStart.checkYourAnswersLabel",
            dateAsDayMonthAndYear(LocalDate.now().plusDays(1)),
            None,
            actions = Actions(items = Seq(ActionItem(
              href = controllers.add.routes.AuthorityStartController.onPageLoad(CheckMode).url,
              content = span(messages("site.change")),
              visuallyHiddenText = Some(messages("checkYourAnswers.authorityStart.hidden"))
            )))
          ),
          summaryListRow(
            "showBalance.checkYourAnswersLabel",
            "showBalance.checkYourAnswers.yes",
            None,
            actions = Actions(items = Seq(ActionItem(
              href = controllers.add.routes.ShowBalanceController.onPageLoad(CheckMode).url,
              content = span(messages("site.change")),
              visuallyHiddenText = Some(messages("checkYourAnswers.showBalance.hidden"))
            )))
          )
        )
      }
    }

    "produce correct authorisation declaration message" when {

      "a single account is selected" in {
        val userAnswers: UserAnswers = userAnswersTodayToIndefinite.set(AccountsPage, List(cashAccount)).success.value
        val helper: CheckYourAnswersHelper = CheckYourAnswersHelper(userAnswers, mockDateTimeService)
        helper.accountsTitle mustBe "checkYourAnswers.accounts.h2.singular"
      }

      "multiple accounts are selected" in {
        val helper: CheckYourAnswersHelper = CheckYourAnswersHelper(userAnswersTodayToIndefinite, mockDateTimeService)
        helper.accountsTitle mustBe "checkYourAnswers.accounts.h2.plural"
      }
    }
  }
}
