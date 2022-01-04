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
import models.UserAnswers
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.ActionItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Actions

class CheckYourAnswersRemoveHelper(val userAnswers: UserAnswers,
                                   val accountId: String,
                                   val authorityId: String,
                                   authorisedUser: AuthorisedUser,
                                   val standingAuthority: StandingAuthority,
                                   account: AccountWithAuthoritiesWithId)(implicit val messages: Messages) extends SummaryListRowHelper {


  def authorisedCompanyDetailsRows: Seq[SummaryListRow] = {
    Seq(
      eoriNumberRow(Some(standingAuthority.authorisedEori))
    ).flatten
  }

  def accountNumberRows: Seq[SummaryListRow] = {
    Seq(
      accountNumberRow(account)
    ).flatten
  }

  def authorisedUserDetailsRows: Seq[SummaryListRow] = {
    Seq(
      Some(authorisedUserNameRow(authorisedUser)),
      Some(authorisedUserRoleRow(authorisedUser))
    ).flatten
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

  private def authorisedUserNameRow(authorisedUser: AuthorisedUser): SummaryListRow = {
    summaryListRow(
      messages("remove-cya-name"),
      value = authorisedUser.userName,
      actions = Actions(items = Seq(ActionItem(
        href = controllers.remove.routes.RemoveAuthorisedUserController.onPageLoad(accountId, authorityId).url,
        content = span(messages("site.change")),
        visuallyHiddenText = Some(messages("remove-cya-visually-hidden-name"))
      ))),
      secondValue = None
    )
  }

  private def authorisedUserRoleRow(authorisedUser: AuthorisedUser): SummaryListRow = {
    summaryListRow(
      messages("remove-cya-role"),
      value = authorisedUser.userRole,
      actions = Actions(items = Seq(ActionItem(
        href = controllers.remove.routes.RemoveAuthorisedUserController.onPageLoad(accountId, authorityId).url,
        content = span(messages("site.change")),
        visuallyHiddenText = Some(messages("remove-cya-visually-hidden-role"))
      ))),
      secondValue = None
    )
  }


}
