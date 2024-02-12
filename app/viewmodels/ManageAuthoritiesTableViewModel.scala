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
  AccountNumber, AccountStatusClosed, AccountStatusPending, AccountStatusSuspended, AccountType,
  AccountWithAuthoritiesWithId, CDSAccountStatus, CdsDutyDefermentAccount, StandingAuthority
}
import play.api.i18n.Messages
import play.api.mvc.Call
import utils.StringUtils.emptyString

import java.time.LocalDate
import scala.collection.immutable.ListMap

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
                                       href: Option[Call] = None,
                                       isAccountStatusNonClosed: Boolean = true,
                                       classValue: String = "govuk-table__cell",
                                       spanClassValue: String = "hmrc-responsive-table__heading")

case class AuthorityRowViewModel(authorisedEori: AuthorityRowColumnViewModel,
                                 formattedFromDate: AuthorityRowColumnViewModel,
                                 formattedToDate: AuthorityRowColumnViewModel,
                                 viewBalanceAsString: AuthorityRowColumnViewModel,
                                 viewLink: AuthorityRowColumnViewModel)

case class ManageAuthoritiesTableViewModel(idString: String,
                                           accountHeadingMsg: String,
                                           authHeaderRowViewModel: AuthorityHeaderRowViewModel,
                                           authRowsView: Seq[AuthorityRowViewModel])

object ManageAuthoritiesTableViewModel {
  def apply(accountId: String,
            account: AccountWithAuthoritiesWithId,
            isNiAccount: Boolean = false)(implicit messages: Messages): ManageAuthoritiesTableViewModel = {

    val idString = s"${account.accountType}-${account.accountNumber}"

    val accountHeadingMsg: String = account.accountStatus match {
      case Some(status) => accountMsgForAccountStatus(status, account, isNiAccount)
      case _ => messages(s"manageAuthorities.table.heading.account.${account.accountType}", account.accountNumber)
    }

    ManageAuthoritiesTableViewModel(
      idString,
      accountHeadingMsg,
      authorityHeaderRowViewModel(account),
      prepareAuthRowsView(accountId, account)
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
      hiddenActionsId = s"account-authorities-table-actions-heading-${account.accountType}-${account.accountNumber}",
      hiddenActionsHeaderValue = messages("manageAuthorities.table.heading.actions")
    )

  private def prepareAuthRowsView(accountId: String,
                                  account: AccountWithAuthoritiesWithId)
                                 (implicit messages: Messages): Seq[AuthorityRowViewModel] = {

    val sortedAuthorities: ListMap[String, StandingAuthority] =
      ListMap(account.authorities.toSeq.sortBy(_._2.authorisedFromDate): _*)

    val authRows = for {
      (authorityId, authority) <- sortedAuthorities
    } yield {
      AuthorityRowViewModel(
        authorisedEori =
          AuthorityRowColumnViewModel(messages("manageAuthorities.table.heading.user"), authority.authorisedEori),
        formattedFromDate =
          AuthorityRowColumnViewModel(messages("manageAuthorities.table.heading.startDate"),
            dateAsdMMMyyyy(authority.authorisedFromDate)),
        formattedToDate =
          authRowColumnViewForAuthorisedToDate(authority.authorisedToDate),
        viewBalanceAsString =
          authRowColumnViewForViewBalance(authority.viewBalance),
        viewLink =
          authRowColumnViewForLink(
            accountId,
            authorityId,
            authority,
            account.accountStatus,
            account.accountNumber,
            account.accountType)
      )
    }

    authRows.toSeq
  }

  private def authRowColumnViewForAuthorisedToDate(authorisedToDate: Option[LocalDate])
                                                  (implicit messages: Messages): AuthorityRowColumnViewModel = {
    val displayValue = authorisedToDate.fold(messages("manageAuthorities.table.endDate.empty"))(dateAsdMMMyyyy)

    AuthorityRowColumnViewModel(messages("manageAuthorities.table.heading.endDate"), displayValue)
  }

  private def authRowColumnViewForViewBalance(viewBalance: Boolean)
                                             (implicit messages: Messages): AuthorityRowColumnViewModel = {
    val displayValue = if (viewBalance) {
      messages("manageAuthorities.table.viewBalance.yes")
    } else {
      messages("manageAuthorities.table.viewBalance.no")
    }

    AuthorityRowColumnViewModel(messages("manageAuthorities.table.heading.balance"), displayValue)
  }

  private def authRowColumnViewForLink(accountId: String,
                                       authorityId: String,
                                       authority: StandingAuthority,
                                       accountStatus: Option[CDSAccountStatus],
                                       accountNumber: AccountNumber,
                                       accountType: AccountType)
                                      (implicit messages: Messages): AuthorityRowColumnViewModel = {

    val displayValue = s"${
      messages("manageAuthorities.table.row.viewLink", authority.authorisedEori, accountNumber)
    } ${
      messages(s"manageAuthorities.table.heading.account.$accountType", accountNumber)
    }"

    val hrefValue = controllers.routes.ViewAuthorityController.onPageLoad(accountId, authorityId)

    val isAccStatClosed: Boolean = accountStatus.fold(true)(_ != AccountStatusClosed)

    AuthorityRowColumnViewModel(
      hiddenHeadingMsg = messages("manageAuthorities.table.view-or-change"),
      displayValue = displayValue,
      href = Some(hrefValue),
      isAccountStatusNonClosed = isAccStatClosed,
      classValue = "govuk-table__cell view-or-change",
      spanClassValue = "govuk-visually-hidden")
  }

  private def dateAsdMMMyyyy(date: LocalDate)(implicit messages: Messages): String =
    s"${date.getDayOfMonth} ${dateAsMonthAbbr(date)} ${date.getYear}"

  private def dateAsMonthAbbr(date: LocalDate)(implicit messages: Messages): String =
    messages(s"month.abbr.${date.getMonthValue}")
}
