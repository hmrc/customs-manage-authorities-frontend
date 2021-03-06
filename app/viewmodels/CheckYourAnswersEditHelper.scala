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

import models.domain.{AccountWithAuthoritiesWithId, AuthorisedUser, StandingAuthority}
import models.{AuthorityStart, ShowBalance, UserAnswers}
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
                                 account: AccountWithAuthoritiesWithId)(implicit val messages: Messages) extends SummaryListRowHelper with DateUtils {

  def yourAccountRow: Seq[SummaryListRow] = {
    Seq(
      accountNumberRow(account)
    ).flatten
  }

  def authorisedCompanyDetailsRows: Seq[SummaryListRow] = {
    Seq(
      eoriNumberRow(Some(standingAuthority.authorisedEori))
    ).flatten
  }

  def authorityDurationRows: Seq[SummaryListRow] = {
    Seq(authorityStartRow,
      showBalanceRow(userAnswers.get(EditShowBalancePage(accountId, authorityId)))
    ).flatten
  }

  def authorisedUserRows: Seq[SummaryListRow] = {
    Seq(
      authorisedUserNameRow(userAnswers.get(EditAuthorisedUserPage(accountId, authorityId))),
      authorisedUserRoleRow(userAnswers.get(EditAuthorisedUserPage(accountId, authorityId)))
    ).flatten
  }

  private def authorisedUserNameRow(authorisedUser: Option[AuthorisedUser]): Option[SummaryListRow] = {
    authorisedUser.map { user =>
      summaryListRow(
        messages("edit-cya-name"),
        value = user.userName,
        actions = Actions(items = Seq(ActionItem(
          href = controllers.edit.routes.EditAuthorisedUserController.onPageLoad(accountId, authorityId).url,
          content = span(messages("site.change")),
          visuallyHiddenText = Some(messages("edit-cya-visually-hidden-name"))
        ))),
        secondValue = None
      )
    }
  }

  private def authorisedUserRoleRow(authorisedUser: Option[AuthorisedUser]): Option[SummaryListRow] = {
    authorisedUser.map { user =>
      summaryListRow(
        messages("edit-cya-role"),
        value = user.userRole,
        actions = Actions(items = Seq(ActionItem(
          href = controllers.edit.routes.EditAuthorisedUserController.onPageLoad(accountId, authorityId).url,
          content = span(messages("site.change")),
          visuallyHiddenText = Some(messages("edit-cya-visually-hidden-role"))
        ))),
        secondValue = None
      )
    }
  }

  private def eoriNumberRow(number: Option[String]): Option[SummaryListRow] = {
    number.map(x =>
      summaryListRow(
        messages("checkYourAnswers.eoriNumber.label"),
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
}
