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

import models.domain.{AccountWithAuthoritiesWithId, StandingAuthority}
import models.{AuthorityEnd, AuthorityStart, CompanyInformation, ShowBalance, ShowBalanceError, UserAnswers}
import pages.edit._
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import services.DateTimeService
import uk.gov.hmrc.govukfrontend.views.Aliases.ActionItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Actions
import utils.DateUtils


class CheckYourAnswersEditHelper(val userAnswers: UserAnswers,
                                 accountId: String,
                                 authorityId: String,
                                 dateTimeService: DateTimeService,
                                 val standingAuthority: StandingAuthority,
                                 account: AccountWithAuthoritiesWithId,
                                 companyInformation: Option[CompanyInformation] = None)(implicit val messages: Messages) extends SummaryListRowHelper with DateUtils {

  def rows: Seq[SummaryListRow] = {
    Seq(
      accountNumberRow,
      eoriNumberRow(Some(standingAuthority.authorisedEori)),
      authorityStartRow,
      authorityEndRow,
      showBalanceRow(userAnswers.get(EditShowBalancePage(accountId, authorityId)))
    ).flatten
  }

  def yourAccountRow: Seq[SummaryListRow] = {
    Seq(
      accountNumberRow
    ).flatten
  }

  def authorisedCompanyDetailsRows: Seq[SummaryListRow] = {
    Seq(
      eoriNumberRow(Some(standingAuthority.authorisedEori)),
      Some(companyNameRow(companyInformation)),
      Some(companyAddressRow(companyInformation))
    ).flatten
  }

  def authorityDurationRows: Seq[SummaryListRow] = {
    Seq(authorityStartRow,
      authorityEndRow,
      showBalanceRow(userAnswers.get(EditShowBalancePage(accountId, authorityId)))
    ).flatten
  }

  private def accountNumberRow: Option[SummaryListRow] = {
    val accountType = messages(s"manageAuthorities.table.heading.account.${account.accountType}", account.accountNumber)


    Some(summaryListRow(
      messages("Account"),
      value = HtmlFormat.escape(accountType).toString,
      actions = Actions(items = Seq.empty),
      secondValue = None
    ))
  }

  private def eoriNumberRow(number: Option[String]): Option[SummaryListRow] = {
    number.map(x =>
      summaryListRow(
        messages("eoriNumber.checkYourAnswersLabel"),
        value = HtmlFormat.escape(x).toString(),
        actions = Actions(items = Seq()),
        secondValue = None
      )
    )
  }

  private def authorityStartRow: Option[SummaryListRow] = {
    userAnswers.get(EditAuthorityStartPage(accountId, authorityId)).flatMap {
      case AuthorityStart.Today => Some(s"${messages("authorityStart.checkYourAnswersLabel.today")} ${dateAsDayMonthAndYear(dateTimeService.localTime().toLocalDate)}")
      case AuthorityStart.Setdate => userAnswers.get(EditAuthorityStartDatePage(accountId, authorityId)).map(dateAsDayMonthAndYear)
    }.map { date =>
      summaryListRow(
        messages("authorityStart.checkYourAnswersLabel"),
        value = date,
        secondValue = None,
        actions = if (standingAuthority.canEditStartDate(dateTimeService.localDate())) {
          Actions(items =
            Seq(ActionItem(
              href = controllers.edit.routes.EditAuthorityStartController.onPageLoad(accountId, authorityId).url,
              content = span(messages("site.change")),
              visuallyHiddenText = Some(messages("checkYourAnswers.authorityStart.hidden"))
            ))
          )
        } else {
          Actions(items = Seq())
        })
    }
  }

  def changedDetails: Boolean = {
    val changes = userAnswers.get(EditShowBalancePage(accountId, authorityId)) match {
      case Some(value) =>
        Right(List(
          standingAuthority.endChanged(userAnswers, accountId, authorityId, dateTimeService.localDate()),
          standingAuthority.startChanged(userAnswers, accountId, authorityId, dateTimeService.localDate()).contains(true),
          balanceRowChanged(value)
        ).contains(true))
      case None => Left(ShowBalanceError)
    }

    changes match {
      case Right(details) => details
      case Left(_) => false
    }
  }


  private def balanceRowChanged(showBalance: ShowBalance): Boolean = {
    showBalance match {
      case ShowBalance.Yes if standingAuthority.viewBalance => false
      case ShowBalance.No if !standingAuthority.viewBalance => false
      case _ => true
    }
  }

  private def authorityEndRow: Option[SummaryListRow] = {
    userAnswers.get(EditAuthorityEndPage(accountId, authorityId)).flatMap {
      case AuthorityEnd.Indefinite => Some(messages("checkYourAnswers.authorityEnd.indefinite"))
      case AuthorityEnd.Setdate => userAnswers.get(EditAuthorityEndDatePage(accountId, authorityId)).map(dateAsDayMonthAndYear)
    }.map(value =>
      summaryListRow(
        messages("authorityEnd.checkYourAnswersLabel"),
        value = value,
        secondValue = None,
        actions = Actions(items = Seq(ActionItem(
          href = controllers.edit.routes.EditAuthorityEndController.onPageLoad(accountId, authorityId).url,
          content = span(messages("site.change")),
          visuallyHiddenText = Some(messages("checkYourAnswers.authorityEnd.hidden"))
        )))
      )
    )
  }

  private def showBalanceRow(maybeBalance: Option[ShowBalance]): Option[SummaryListRow] = {
    maybeBalance.map(value =>
      summaryListRow(
        messages("showBalance.checkYourAnswersLabel"),
        value = messages(s"showBalance.checkYourAnswers.$value"),
        secondValue = None,
        actions = Actions(items = Seq(ActionItem(
          href = controllers.edit.routes.EditShowBalanceController.onPageLoad(accountId, authorityId).url,
          content = span(messages("site.change")),
          visuallyHiddenText = Some(messages("checkYourAnswers.showBalance.hidden"))
        )))
      )
    )
  }

  //TODO add message keys
  private def companyNameRow(maybeCompanyInformation: Option[CompanyInformation]): SummaryListRow = {
    maybeCompanyInformation match {
      case Some(value) =>
        summaryListRow(
          messages("Name"),
          value = value.name,
          actions = Actions(items = Seq()),
          secondValue = None
        )
      case None =>       summaryListRow(
        messages("Name"),
        value = "Not available",
        actions = Actions(items = Seq()),
        secondValue = None
      )
    }
  }

  //TODO add message keys & ensure correct formatting on address
  private def companyAddressRow(maybeCompanyInformation: Option[CompanyInformation]): SummaryListRow = {
    maybeCompanyInformation match {
      case Some(value) =>
        summaryListRow(
          messages("Address"),
          value = value.address,
          actions = Actions(items = Seq()),
          secondValue = None
        )
      case None =>       summaryListRow(
        messages("Address"),
        value = "Not available",
        actions = Actions(items = Seq()),
        secondValue = None
      )
    }
  }
}
