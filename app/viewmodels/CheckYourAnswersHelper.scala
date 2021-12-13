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

package viewmodels

import models.domain.{AuthorityDetails, CDSAccount}
import models.{AuthorityStart, CheckMode, ShowBalance, UserAnswers}
import pages.add._
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import services.DateTimeService
import uk.gov.hmrc.govukfrontend.views.Aliases.ActionItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Actions
import viewmodels.ManageAuthoritiesViewModel.dateAsDayMonthAndYear

case class CheckYourAnswersHelper (userAnswers: UserAnswers, dateTimeService: DateTimeService)
                            (implicit val messages: Messages) extends SummaryListRowHelper {

  private val selectedAccounts: List[CDSAccount] = userAnswers.get(AccountsPage).getOrElse(Nil)

  val authorisationDeclaration: String = selectedAccounts.size match {
    case 1 => messages("authorisedUser.declaration.singular")
    case _ => messages("authorisedUser.declaration.plural")
  }

  def accountsRows: Seq[SummaryListRow] = {
    Seq(
      Some(accountsRow(selectedAccounts)),
    ).flatten
  }

  def companyDetailsRows: Seq[SummaryListRow] = {
    Seq(
      eoriNumberRow(userAnswers.get(EoriNumberPage))
    ).flatten
  }

  def authorityDurationRows: Seq[SummaryListRow] = {
    Seq(
      authorityStartRow(userAnswers),
      showBalanceRow(userAnswers.get(ShowBalancePage))
    ).flatten
  }

  def authorityDetailsRows: Seq[SummaryListRow] = {
    yourDetailsRows(userAnswers.get(AuthorityDetailsPage))
  }

  private def accountsRow(selectedAccounts: List[CDSAccount]): SummaryListRow = {
    val list = selectedAccounts.map { account =>
      val cdsAccount = account
      s"${messages("accounts.type." + account.accountType)}: ${cdsAccount.number}"
    }

    summaryListRow(
      if (list.size == 1) messages("accounts.checkYourAnswersLabel.singular") else messages("accounts.checkYourAnswersLabel.plural"),
      value = list.mkString("<br>"),
      actions = Actions(items = Seq(ActionItem(
        href = controllers.add.routes.AccountsController.onPageLoad(CheckMode).url,
        content = span(messages("site.change")),
        visuallyHiddenText = Some(messages("checkYourAnswers.accounts.hidden"))
      ))),
      secondValue = None)
  }

  private def eoriNumberRow(number: Option[String]): Option[SummaryListRow] = {
    number.map(x =>
      summaryListRow(
        messages("eoriNumber.checkYourAnswersLabel"),
        value = HtmlFormat.escape(x).toString(),
        actions = Actions(items = Seq(ActionItem(
          href = controllers.add.routes.EoriNumberController.onPageLoad(CheckMode).url,
          content = span(messages("site.change")),
          visuallyHiddenText = Some(messages("checkYourAnswers.eoriNumber.hidden"))
        ))),
        secondValue = None
      )
    )
  }


  private def yourDetailsRows(authorityDetails: Option[AuthorityDetails]): Seq[SummaryListRow] = {
      Seq(summaryListRow(
        messages("checkYourAnswers.fullName"),
        value = HtmlFormat.escape(authorityDetails.get.userName).toString(),
        actions = Actions(items = Seq(ActionItem(
          href = controllers.add.routes.AuthorityStartController.onPageLoad(CheckMode).url,
          content = span(messages("site.change")),
          visuallyHiddenText = Some(messages("checkYourAnswers.eoriNumber.hidden"))
        ))),
        secondValue = None
      ),
        summaryListRow(
        messages("checkYourAnswers.role"),
        value = HtmlFormat.escape(authorityDetails.get.userRole).toString(),
        actions = Actions(items = Seq(ActionItem(
          href = controllers.add.routes.AuthorityStartController.onPageLoad(CheckMode).url,
          content = span(messages("site.change")),
          visuallyHiddenText = Some(messages("checkYourAnswers.eoriNumber.hidden"))
        ))),
        secondValue = None
      )
      )
  }

  private def authorityStartRow(userAnswers: UserAnswers): Option[SummaryListRow] = {
    userAnswers.get(AuthorityStartPage).flatMap {
      case AuthorityStart.Today => Some(s"${messages("authorityStart.checkYourAnswersLabel.today")} ${dateAsDayMonthAndYear(dateTimeService.localTime().toLocalDate)}")
      case AuthorityStart.Setdate => userAnswers.get(AuthorityStartDatePage).map(dateAsDayMonthAndYear)
    }.map(date =>
      summaryListRow(
        messages("authorityStart.checkYourAnswersLabel"),
        value = date,
        actions = Actions(items = Seq(ActionItem(
          href = controllers.add.routes.AuthorityStartController.onPageLoad(CheckMode).url,
          content = span(messages("site.change")),
          visuallyHiddenText = Some(messages("checkYourAnswers.authorityStart.hidden"))
        ))),
        secondValue = None
      )
    )
  }

  private def showBalanceRow(maybeBalance: Option[ShowBalance]): Option[SummaryListRow] = {
    maybeBalance.map(value =>
      summaryListRow(
        messages("showBalance.checkYourAnswersLabel"),
        value = messages(s"showBalance.checkYourAnswers.$value"),
        actions = Actions(items = Seq(ActionItem(
          href = controllers.add.routes.ShowBalanceController.onPageLoad(CheckMode).url,
          content = span(messages("site.change")),
          visuallyHiddenText = Some(messages("checkYourAnswers.showBalance.hidden"))
        ))),
        secondValue = None
      )
    )
  }
}
