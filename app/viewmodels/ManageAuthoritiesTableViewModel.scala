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
import utils.DateUtils

import scala.collection.immutable.ListMap

case class AuthorityRowColumnViewModel(hiddenHeadingMsg: String,
                                       displayValue: String,
                                       href: Option[Call] = None,
                                       isAccountStatusNonClosed: Boolean = true,
                                       classValue: String = "govuk-table__cell",
                                       spanClassValue: String = "hmrc-responsive-table__heading")

case class AuthorityRowViewModel(authorisedEori: AuthorityRowColumnViewModel,
                                 companyName: Option[AuthorityRowColumnViewModel] = None,
                                 viewLink: AuthorityRowColumnViewModel)

case class ManageAuthoritiesTableViewModel(idString: String,
                                           accountHeadingMsg: String,
                                           authRows: Seq[AuthorityRowViewModel])

object ManageAuthoritiesTableViewModel extends DateUtils {
  def apply(accountId: String,
            account: AccountWithAuthoritiesWithId,
            isNiAccount: Boolean = false,
            authorisedEoriAndCompanyMap: Map[String, String] = Map.empty)
           (implicit messages: Messages): ManageAuthoritiesTableViewModel = {

    val idString = s"${account.accountType}-${account.accountNumber}"

    val accountHeadingMsg: String = account.accountStatus match {
      case Some(status) => accountMsgForAccountStatus(status, account, isNiAccount)
      case _ => messages(s"manageAuthorities.table.heading.account.${account.accountType}", account.accountNumber)
    }

    ManageAuthoritiesTableViewModel(
      idString,
      accountHeadingMsg,
      prepareAuthRowsView(accountId, account, authorisedEoriAndCompanyMap)
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

  private def prepareAuthRowsView(accountId: String,
                                  account: AccountWithAuthoritiesWithId,
                                  authorisedEoriAndCompanyMap: Map[String, String])
                                 (implicit messages: Messages): Seq[AuthorityRowViewModel] = {

    val sortedAuthorities: ListMap[String, StandingAuthority] =
      ListMap(account.authorities.toSeq.sortBy(_._2.authorisedFromDate): _*)

    val authRows = for {
      (authorityId, authority) <- sortedAuthorities
    } yield {
      AuthorityRowViewModel(
        authorisedEori =
          AuthorityRowColumnViewModel(messages("manageAuthorities.table.heading.user"), authority.authorisedEori),
        companyName =
          if (authorisedEoriAndCompanyMap.contains(authority.authorisedEori)) {
            Some(AuthorityRowColumnViewModel(messages("manageAuthorities.table.heading.user"),
              authorisedEoriAndCompanyMap(authority.authorisedEori)))
          } else {
            None
          },
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
}
