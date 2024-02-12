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

import models.domain._
import utils.ViewTestHelper
import viewmodels.ManageAuthoritiesViewModel._

import java.time.LocalDate

class ManageAuthoritiesTableViewModelSpec extends ViewTestHelper {

  "View model" should {

    "contain correct accountHeadingMsg" when {

      "account status is closed" in new Setup {
        private val viewModelOb = ManageAuthoritiesTableViewModel(accountId, closedCashAccWithAuthoritiesWithId)

        viewModelOb.accountHeadingMsg mustBe messages(
          "manageAuthorities.table.heading.account.CdsCashAccount.closed", accountNumber)
      }

      "account status is pending" in new Setup {
        private val viewModelOb = ManageAuthoritiesTableViewModel(accountId, pendingCashAccWithAuthoritiesWithId)

        viewModelOb.accountHeadingMsg mustBe messages(
          "manageAuthorities.table.heading.account.CdsCashAccount.pending", accountNumber)
      }

      "account status is suspended" in new Setup {
        private val viewModelOb = ManageAuthoritiesTableViewModel(accountId, suspendedCashAccWithAuthoritiesWithId)

        viewModelOb.accountHeadingMsg mustBe messages(
          "manageAuthorities.table.heading.account.CdsCashAccount.suspended", accountNumber)
      }

      "account is of Northern Ireland and account type is Duty Deferment" in new Setup {
        private val viewModelOb =
          ManageAuthoritiesTableViewModel(accountId, openDDAccWithAuthoritiesWithId, isNiAccount = true)

        viewModelOb.accountHeadingMsg mustBe messages(
          "manageAuthorities.table.heading.account.CdsDutyDefermentAccount.Ni", accountNumber)
      }

      "account is non Northern Ireland and account type is Duty Deferment" in new Setup {
        private val viewModelOb =
          ManageAuthoritiesTableViewModel(accountId, openDDAccWithAuthoritiesWithId)

        viewModelOb.accountHeadingMsg mustBe messages(
          "manageAuthorities.table.heading.account.CdsDutyDefermentAccount", accountNumber)
      }

      "account status is None" in new Setup {
        private val viewModelOb =
          ManageAuthoritiesTableViewModel(accountId, noneDDAccWithAuthoritiesWithId)

        viewModelOb.accountHeadingMsg mustBe messages(
          "manageAuthorities.table.heading.account.CdsDutyDefermentAccount", accountNumber)
      }

      "CdsCashAccount status is open" in new Setup {
        private val viewModelOb =
          ManageAuthoritiesTableViewModel(accountId, openCashAccWithAuthoritiesWithId)

        viewModelOb.accountHeadingMsg mustBe messages(
          "manageAuthorities.table.heading.account.CdsCashAccount", accountNumber)
      }

      "CdsGeneralGuaranteeAccount status is open" in new Setup {
        private val viewModelOb =
          ManageAuthoritiesTableViewModel(accountId, openGGAccWithAuthoritiesWithId)

        viewModelOb.accountHeadingMsg mustBe messages(
          "manageAuthorities.table.heading.account.CdsGeneralGuaranteeAccount", accountNumber)
      }
    }

    "contain correct authority header row" when {

      "CdsGeneralGuaranteeAccount status is open" in new Setup {
        private val viewModelOb =
          ManageAuthoritiesTableViewModel(accountId, openGGAccWithAuthoritiesWithId)

        val expectedRowViewModel: AuthorityHeaderRowViewModel = AuthorityHeaderRowViewModel(
          authCompanyId = s"account-authorities-table-user-heading-CdsGeneralGuaranteeAccount-$accountNumber",
          authCompanyHeaderValue = messages("manageAuthorities.table.heading.user"),
          startDateId = s"account-authorities-table-start-date-heading-CdsGeneralGuaranteeAccount-$accountNumber",
          startDateHeader = messages("manageAuthorities.table.heading.startDate"),
          endDateId = s"account-authorities-table-end-date-heading-CdsGeneralGuaranteeAccount-$accountNumber",
          endDateHeader = messages("manageAuthorities.table.heading.endDate"),
          viewBalanceId = s"account-authorities-table-view-balance-heading-CdsGeneralGuaranteeAccount-$accountNumber",
          viewBalanceHeaderValue = messages("manageAuthorities.table.heading.balance"),
          hiddenActionsId = s"account-authorities-table-actions-heading--CdsGeneralGuaranteeAccount-$accountNumber",
          hiddenActionsHeaderValue = messages("manageAuthorities.table.heading.actions")
        )

        viewModelOb.authHeaderRowViewModel mustBe expectedRowViewModel
      }
    }

    "contain correct authority rows" when {

      "account status is open" in new Setup {
        private val viewModelOb =
          ManageAuthoritiesTableViewModel(accountId, openGGAccWithAuthoritiesWithId)

        val expectedAuthRowsView: Seq[AuthorityRowViewModel] = Seq(
          AuthorityRowViewModel(
            authorisedEori =
              AuthorityRowColumnViewModel(messages("manageAuthorities.table.heading.user"), eoriNumber),
            formattedFromDate =
              AuthorityRowColumnViewModel(
                messages("manageAuthorities.table.heading.startDate"),
                dateAsdMMMyyyy(startDate1)
              ),
            formattedToDate =
              AuthorityRowColumnViewModel(messages("manageAuthorities.table.heading.endDate"), dateAsdMMMyyyy(endDate1)),
            viewBalanceAsString =
              AuthorityRowColumnViewModel(
                messages("manageAuthorities.table.heading.balance"),
                messages("manageAuthorities.table.viewBalance.no")
              ),
            viewLink = AuthorityRowColumnViewModel(
              messages("manageAuthorities.table.view-or-change"),
              s"${messages("manageAuthorities.table.row.viewLink", eoriNumber, accountNumber)} ${
                messages(s"manageAuthorities.table.heading.account.CdsGeneralGuaranteeAccount", accountNumber)
              }",
              href = Some(controllers.routes.ViewAuthorityController.onPageLoad(accountId, authIdb)),
              classValue = "govuk-table__cell view-or-change",
              spanClassValue = "govuk-visually-hidden")
          ),
          AuthorityRowViewModel(
            authorisedEori =
              AuthorityRowColumnViewModel(messages("manageAuthorities.table.heading.user"), eoriNumber),
            formattedFromDate =
              AuthorityRowColumnViewModel(
                messages("manageAuthorities.table.heading.startDate"),
                dateAsdMMMyyyy(startDate2)
              ),
            formattedToDate =
              AuthorityRowColumnViewModel(
                messages("manageAuthorities.table.heading.endDate"),
                dateAsdMMMyyyy(endDate2)
              ),
            viewBalanceAsString =
              AuthorityRowColumnViewModel(
                messages("manageAuthorities.table.heading.balance"),
                messages("manageAuthorities.table.viewBalance.no")
              ),
            viewLink = AuthorityRowColumnViewModel(
              messages("manageAuthorities.table.view-or-change"),
              s"${messages("manageAuthorities.table.row.viewLink", eoriNumber, accountNumber)} ${
                messages(s"manageAuthorities.table.heading.account.CdsGeneralGuaranteeAccount", accountNumber)
              }",
              href = Some(controllers.routes.ViewAuthorityController.onPageLoad(accountId, authIdc)),
              classValue = "govuk-table__cell view-or-change",
              spanClassValue = "govuk-visually-hidden")
          )
        )

        viewModelOb.authRowsView mustBe expectedAuthRowsView
      }

      "account status is closed" in new Setup {
        private val viewModelOb =
          ManageAuthoritiesTableViewModel(accountId, closedGGAccWithAuthoritiesWithId)

        val expectedAuthRowsView: Seq[AuthorityRowViewModel] = Seq(
          AuthorityRowViewModel(
            authorisedEori =
              AuthorityRowColumnViewModel(messages("manageAuthorities.table.heading.user"), eoriNumber),
            formattedFromDate =
              AuthorityRowColumnViewModel(
                messages("manageAuthorities.table.heading.startDate"),
                dateAsdMMMyyyy(startDate1)
              ),
            formattedToDate =
              AuthorityRowColumnViewModel(
                messages("manageAuthorities.table.heading.endDate"),
                dateAsdMMMyyyy(endDate1)
              ),
            viewBalanceAsString =
              AuthorityRowColumnViewModel(
                messages("manageAuthorities.table.heading.balance"), messages("manageAuthorities.table.viewBalance.no")),
            viewLink = AuthorityRowColumnViewModel(
              messages("manageAuthorities.table.view-or-change"),
              s"${messages("manageAuthorities.table.row.viewLink", eoriNumber, accountNumber)} ${
                messages(s"manageAuthorities.table.heading.account.CdsGeneralGuaranteeAccount", accountNumber)
              }",
              href = Some(controllers.routes.ViewAuthorityController.onPageLoad(accountId, authIdb)),
              isAccountStatusNonClosed = false,
              classValue = "govuk-table__cell view-or-change",
              spanClassValue = "govuk-visually-hidden")
          ),
          AuthorityRowViewModel(
            authorisedEori =
              AuthorityRowColumnViewModel(messages("manageAuthorities.table.heading.user"), eoriNumber),
            formattedFromDate =
              AuthorityRowColumnViewModel(
                messages("manageAuthorities.table.heading.startDate"),
                dateAsdMMMyyyy(startDate1)
              ),
            formattedToDate =
              AuthorityRowColumnViewModel(
                messages("manageAuthorities.table.heading.endDate"),
                dateAsdMMMyyyy(endDate1)
              ),
            viewBalanceAsString =
              AuthorityRowColumnViewModel(
                messages("manageAuthorities.table.heading.balance"),
                messages("manageAuthorities.table.viewBalance.yes")
              ),
            viewLink = AuthorityRowColumnViewModel(
              messages("manageAuthorities.table.view-or-change"),
              s"${messages("manageAuthorities.table.row.viewLink", eoriNumber, accountNumber)} ${
                messages(s"manageAuthorities.table.heading.account.CdsGeneralGuaranteeAccount", accountNumber)
              }",
              href = Some(controllers.routes.ViewAuthorityController.onPageLoad(accountId, authIdc)),
              isAccountStatusNonClosed = false,
              classValue = "govuk-table__cell view-or-change",
              spanClassValue = "govuk-visually-hidden")
          )
        )

        viewModelOb.authRowsView mustBe expectedAuthRowsView
      }
    }
  }

  trait Setup {

    val accountId = "a"
    val authIdb = "b"
    val authIdc = "c"

    val accountNumber = "12345678"
    val eoriNumber = "EORI"

    val startDate1: LocalDate = LocalDate.parse("2020-03-01")
    val startDate2: LocalDate = LocalDate.parse("2020-04-01")
    val endDate1: LocalDate = LocalDate.parse("2020-04-01")
    val endDate2: LocalDate = LocalDate.parse("2020-05-01")

    val standingAuthority1: StandingAuthority =
      StandingAuthority(eoriNumber, startDate1, Some(endDate1), viewBalance = false)

    val standingAuthority2: StandingAuthority =
      StandingAuthority(eoriNumber, startDate2, Some(endDate2), viewBalance = false)

    val standingAuthority3: StandingAuthority =
      StandingAuthority(eoriNumber, startDate1, Some(endDate1), viewBalance = true)

    val openCashAccWithAuthoritiesWithId: AccountWithAuthoritiesWithId = AccountWithAuthoritiesWithId(
      CdsCashAccount,
      accountNumber,
      Some(AccountStatusOpen),
      Map(authIdb -> standingAuthority1, authIdc -> standingAuthority2))

    val closedCashAccWithAuthoritiesWithId: AccountWithAuthoritiesWithId = AccountWithAuthoritiesWithId(
      CdsCashAccount,
      accountNumber,
      Some(AccountStatusClosed),
      Map(authIdb -> standingAuthority1, authIdc -> standingAuthority2))

    val pendingCashAccWithAuthoritiesWithId: AccountWithAuthoritiesWithId = AccountWithAuthoritiesWithId(
      CdsCashAccount,
      accountNumber,
      Some(AccountStatusPending),
      Map(authIdb -> standingAuthority1, authIdc -> standingAuthority2))

    val suspendedCashAccWithAuthoritiesWithId: AccountWithAuthoritiesWithId = AccountWithAuthoritiesWithId(
      CdsCashAccount,
      accountNumber,
      Some(AccountStatusSuspended),
      Map(authIdb -> standingAuthority1, authIdc -> standingAuthority2))

    val openDDAccWithAuthoritiesWithId: AccountWithAuthoritiesWithId = AccountWithAuthoritiesWithId(
      CdsDutyDefermentAccount,
      accountNumber,
      Some(AccountStatusOpen),
      Map(authIdb -> standingAuthority1, authIdc -> standingAuthority2))

    val noneDDAccWithAuthoritiesWithId: AccountWithAuthoritiesWithId = AccountWithAuthoritiesWithId(
      CdsDutyDefermentAccount,
      accountNumber,
      None,
      Map(authIdb -> standingAuthority1, authIdc -> standingAuthority2))

    val openGGAccWithAuthoritiesWithId: AccountWithAuthoritiesWithId = AccountWithAuthoritiesWithId(
      CdsGeneralGuaranteeAccount,
      accountNumber,
      Some(AccountStatusOpen),
      Map(authIdb -> standingAuthority1, authIdc -> standingAuthority2))

    val closedGGAccWithAuthoritiesWithId: AccountWithAuthoritiesWithId = AccountWithAuthoritiesWithId(
      CdsGeneralGuaranteeAccount,
      accountNumber,
      Some(AccountStatusClosed),
      Map(authIdb -> standingAuthority1, authIdc -> standingAuthority3))

    val authoritiesWithId: AuthoritiesWithId = AuthoritiesWithId(Map(
      ("a" ->
        AccountWithAuthoritiesWithId(
          CdsCashAccount,
          "12345",
          Some(AccountStatusOpen),
          Map(authIdb -> standingAuthority1, authIdc -> standingAuthority2)))
    ))

  }
}
