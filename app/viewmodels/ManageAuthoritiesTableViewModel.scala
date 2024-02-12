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

import models.domain.{
  AccountStatusClosed, AccountStatusPending, AccountStatusSuspended,
  AccountWithAuthoritiesWithId, CDSAccountStatus, CdsDutyDefermentAccount
}
import play.api.i18n.Messages
import utils.StringUtils.emptyString

case class AuthorityHeaderRowViewModel(authCompanyId: String,
                                       authCompanyHeaderValue: String,
                                       startDateId: String,
                                       startDateHeader: String,
                                       endDateId: String,
                                       endDateHeader: String,
                                       viewBalanceId: String,
                                       viewBalanceHeaderValue: String,
                                       hiddenActionsId: String,
                                       hiddenActionsHeaderValue: String = emptyString)

case class AuthorityRowColumnViewModel(hiddenHeadingMsg: String,
                                       displayValue: String,
                                       href: Option[String] = None,
                                       classValue: String = "govuk-table__cell",
                                       spanClassValue: String = "hmrc-responsive-table__heading")

case class AuthorityRowViewModel(authorisedEori: AuthorityRowColumnViewModel,
                                 formattedFromDate: AuthorityRowColumnViewModel,
                                 formattedToDate: AuthorityRowColumnViewModel,
                                 viewBalanceAsString: AuthorityRowColumnViewModel,
                                 viewLink: AuthorityRowColumnViewModel)

case class ManageAuthoritiesTableViewModel(accountHeadingMsg: String,
                                           authHeaderRowViewModel: AuthorityHeaderRowViewModel,
                                           authRowsView: Seq[AuthorityRowViewModel])

object ManageAuthoritiesTableViewModel {
  def apply(accountId: String,
            account: AccountWithAuthoritiesWithId,
            isNiAccount: Boolean = false)(implicit messages: Messages): ManageAuthoritiesTableViewModel = {

    val accountHeadingMsg: String = account.accountStatus match {
      case Some(status) => accountMsgForAccountStatus(status, account, isNiAccount)
      case _ => messages(s"manageAuthorities.table.heading.account.${account.accountType}", account.accountNumber)
    }

    ManageAuthoritiesTableViewModel(
      accountHeadingMsg,
      authorityHeaderRowViewModel(account),
      Seq()
    )
  }

  private def accountMsgForAccountStatus(status: CDSAccountStatus,
                                         account: AccountWithAuthoritiesWithId,
                                         isNiAccount: Boolean)(implicit messages: Messages): String =
    status match {
      case AccountStatusClosed =>
        messages(s"manageAuthorities.table.heading.account.${account.accountType}.closed", account.accountNumber)

      case AccountStatusPending =>
        messages(s"manageAuthorities.table.heading.account.${account.accountType}.pending", account.accountNumber)

      case AccountStatusSuspended =>
        messages(s"manageAuthorities.table.heading.account.${account.accountType}.suspended", account.accountNumber)

      case _ =>
        if (isNiAccount && account.accountType == CdsDutyDefermentAccount) {
          messages("manageAuthorities.table.heading.account.CdsDutyDefermentAccount.Ni", account.accountNumber)
        } else {
          messages(s"manageAuthorities.table.heading.account.${account.accountType}", account.accountNumber)
        }
    }

  private def authorityHeaderRowViewModel(account: AccountWithAuthoritiesWithId)(implicit messages: Messages) =
    AuthorityHeaderRowViewModel(
      authCompanyId = s"account-authorities-table-user-heading-${account.accountType}-${account.accountNumber}",
      authCompanyHeaderValue = messages("manageAuthorities.table.heading.user"),
      startDateId = s"account-authorities-table-start-date-heading-${account.accountType}-${account.accountNumber}",
      startDateHeader = messages("manageAuthorities.table.heading.startDate"),
      endDateId = s"account-authorities-table-end-date-heading-${account.accountType}-${account.accountNumber}",
      endDateHeader = messages("manageAuthorities.table.heading.endDate"),
      viewBalanceId = s"account-authorities-table-view-balance-heading-${account.accountType}-${account.accountNumber}",
      viewBalanceHeaderValue = messages("manageAuthorities.table.heading.balance"),
      hiddenActionsId = s"account-authorities-table-actions-heading--${account.accountType}-${account.accountNumber}",
      hiddenActionsHeaderValue = messages("manageAuthorities.table.heading.actions")
    )

}
