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

import models.domain.{AuthorisedUser, CDSAccount}
import models.{AuthorityEnd, AuthorityStart, CheckMode, CompanyDetails, ShowBalance, UserAnswers}
import pages.add._
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import services.DateTimeService
import uk.gov.hmrc.govukfrontend.views.Aliases.ActionItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Actions
import utils.Constants.DUTY_DEFERMENT_ACCOUNT_TYPE
import viewmodels.ManageAuthoritiesViewModel.dateAsDayMonthAndYear

case class CheckYourAnswersHelper(userAnswers: UserAnswers,
                                  dateTimeService: DateTimeService)(implicit val messages: Messages)
  extends SummaryListRowHelper {

  private val selectedAccounts: List[CDSAccount] = userAnswers.get(AccountsPage).getOrElse(Nil)

  val accountsTitle: String = selectedAccounts.size match {
    case 1 => messages("checkYourAnswers.accounts.h2.singular")
    case _ => messages("checkYourAnswers.accounts.h2.plural")
  }

  val companyName: Option[String] = userAnswers.get(EoriNumberPage).map(x => x.name).get

  def companyDetailsRows: Seq[SummaryListRow] = {
    if (companyName.isEmpty) {
      Seq(
        eoriNumberRow(userAnswers.get(EoriNumberPage)),
        companyNameNoConsentRow
      ).flatten
    }
    else {
      Seq(
        eoriNumberRow(userAnswers.get(EoriNumberPage)),
        companyNameRow(userAnswers.get(EoriNumberPage))
      ).flatten
    }
  }

  def accountsRows: Seq[SummaryListRow] = {
    Seq(
      Some(accountsRow(selectedAccounts))
    ).flatten
  }

  def authorityDurationRows: Seq[SummaryListRow] = {
    Seq(
      authorityStartRow(userAnswers),
      authorityEndRow(userAnswers),
      showBalanceRow(userAnswers.get(ShowBalancePage))
    ).flatten
  }

  def authorityDetailsRows: Seq[SummaryListRow] = {
    yourDetailsRows(userAnswers.get(AuthorityDetailsPage))
  }

  private def accountsRow(selectedAccounts: List[CDSAccount]): SummaryListRow = {
    val list = selectedAccounts.map {
      account =>
        if (account.isNiAccount && account.accountType == DUTY_DEFERMENT_ACCOUNT_TYPE) {
          s"${messages("accounts.type." + account.accountType)} ${messages("accounts.ni")}: ${account.number}"
        } else {
          s"${messages("accounts.type." + account.accountType)}: ${account.number}"
        }
    }

    summaryListRow(
      if (list.size == 1) messages("accounts.checkYourAnswersLabel.singular") else messages("accounts.checkYourAnswersLabel.plural"),
      value = list.mkString("<br>"),
      actions =
        Actions(
          items = Seq(ActionItem(
            href = controllers.add.routes.AccountsController.onPageLoad(CheckMode).url,
            content = span(messages("site.change")),
            visuallyHiddenText = Some(messages("checkYourAnswers.accounts.hidden")))
          )
        ),
      secondValue = None)
  }

  private def eoriNumberRow(number: Option[CompanyDetails]): Option[SummaryListRow] = {
    number.map(
      companyDetails =>
        summaryListRow(
          messages("checkYourAnswers.eoriNumber.label"),
          value = HtmlFormat.escape(companyDetails.eori).toString(),
          actions =
            Actions(
              items = Seq(ActionItem(
                href = controllers.add.routes.EoriNumberController.onPageLoad(CheckMode).url,
                content = span(messages("site.change")),
                visuallyHiddenText = Some(messages("checkYourAnswers.eoriNumber.hidden")))
              )
            ),
          secondValue = None
        )
    )
  }

  private def companyNameRow(companyDetails: Option[CompanyDetails]): Option[SummaryListRow] = {
    companyDetails.map(x =>
      summaryListRow(
        messages("checkYourAnswers.companyName.label"),
        value = HtmlFormat.escape(x.name.get).toString(),
        actions = Actions(items = Seq()),
        secondValue = None
      )
    )
  }

  private def companyNameNoConsentRow: Option[SummaryListRow] = {
    Some(summaryListRow(
      messages("view-authority-h2.5"),
      value = messages("view-authority-h2.6"),
      actions = Actions(items = Seq()),
      secondValue = None
    ))
  }

  private def yourDetailsRows(authorityDetails: Option[AuthorisedUser]): Seq[SummaryListRow] = {
    Seq(summaryListRow(
      messages("checkYourAnswers.fullName.label"),
      value = HtmlFormat.escape(authorityDetails.get.userName).toString(),
      actions = Actions(items = Seq(ActionItem(
        href = controllers.add.routes.AuthorityDetailsController.onPageLoad(CheckMode).url,
        content = span(messages("site.change")),
        visuallyHiddenText = Some(messages("checkYourAnswers.fullName.hidden"))
      ))),
      secondValue = None
    ),
      summaryListRow(
        messages("checkYourAnswers.role.label"),
        value = HtmlFormat.escape(authorityDetails.get.userRole).toString(),
        actions = Actions(items = Seq(ActionItem(
          href = controllers.add.routes.AuthorityDetailsController.onPageLoad(CheckMode).url,
          content = span(messages("site.change")),
          visuallyHiddenText = Some(messages("checkYourAnswers.role.hidden"))
        ))),
        secondValue = None
      )
    )
  }

  private def authorityStartRow(userAnswers: UserAnswers): Option[SummaryListRow] = {

    userAnswers.get(AuthorityStartPage).flatMap {
      case AuthorityStart.Today =>
        Some(s"${messages("authorityStart.checkYourAnswersLabel.today")} ${
          dateAsDayMonthAndYear(
            dateTimeService.localTime().toLocalDate)
        }")

      case AuthorityStart.Setdate => userAnswers.get(AuthorityStartDatePage).map(dateAsDayMonthAndYear)
    }.map(
      date =>
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

  private def authorityEndRow(userAnswers: UserAnswers): Option[SummaryListRow] = {
    userAnswers.get(AuthorityEndPage).flatMap {
      case AuthorityEnd.Indefinite => Some(messages("checkYourAnswers.authorityEnd.indefinite"))
      case AuthorityEnd.Setdate => userAnswers.get(AuthorityEndDatePage).map(dateAsDayMonthAndYear)
    }.map(
      value =>
        summaryListRow(
          messages("authorityEnd.checkYourAnswersLabel"),
          value = value,
          actions = Actions(items = Seq(ActionItem(
            href = controllers.add.routes.AuthorityEndController.onPageLoad(CheckMode).url,
            content = span(messages("site.change")),
            visuallyHiddenText = Some(messages("checkYourAnswers.authorityEnd.hidden"))
          ))),
          secondValue = None
        )
    )
  }

  private def showBalanceRow(maybeBalance: Option[ShowBalance]): Option[SummaryListRow] = {
    maybeBalance.map(
      value =>
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
