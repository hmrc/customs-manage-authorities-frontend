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

import models.domain.{AuthorisedUser, StandingAuthority}
import models.{CompanyInformation, UserAnswers}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.ActionItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Actions

class CheckYourAnswersRemoveHelper(val userAnswers: UserAnswers,
                                   accountId: String,
                                   authorityId: String,
                                   authorisedUser: AuthorisedUser,
                                   val standingAuthority: StandingAuthority,
                                   companyInformation: Option[CompanyInformation] = None)(implicit val messages: Messages) extends SummaryListRowHelper {


  def authorisedCompanyDetailsRows: Seq[SummaryListRow] = {
    Seq(
      eoriNumberRow(Some(standingAuthority.authorisedEori)),
      Some(companyNameRow(companyInformation)),
      Some(companyAddressRow(companyInformation))
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
        messages("eoriNumber.checkYourAnswersLabel"),
        value = HtmlFormat.escape(x).toString(),
        actions = Actions(items = Seq()),
        secondValue = None
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
      case None => summaryListRow(
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
          value = value.formattedAddress,
          actions = Actions(items = Seq()),
          secondValue = None
        )
      case None => summaryListRow(
        messages("Address"),
        value = "Not available",
        actions = Actions(items = Seq()),
        secondValue = None
      )
    }
  }

  //TODO ensure visually hidden text is appropriate
  private def authorisedUserNameRow(authorisedUser: AuthorisedUser): SummaryListRow = {
    summaryListRow(
      messages("Your name"),
      value = authorisedUser.userName,
      actions = Actions(items = Seq(ActionItem(
        href = controllers.remove.routes.RemoveAuthorisedUserController.onPageLoad(accountId, authorityId).url,
        content = span(messages("site.change")),
        visuallyHiddenText = Some(messages("checkYourAnswers.authorityEnd.hidden"))
      ))),
      secondValue = None
    )
  }

  private def authorisedUserRoleRow(authorisedUser: AuthorisedUser): SummaryListRow = {
    summaryListRow(
      messages("Your name"),
      value = authorisedUser.userRole,
      actions = Actions(items = Seq(ActionItem(
        href = controllers.remove.routes.RemoveAuthorisedUserController.onPageLoad(accountId, authorityId).url,
        content = span(messages("site.change")),
        visuallyHiddenText = Some(messages("checkYourAnswers.authorityEnd.hidden"))
      ))),
      secondValue = None
    )
  }


}
