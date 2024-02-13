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

import utils.TestData._
import utils.ViewTestHelper
import viewmodels.ManageAuthoritiesViewModel._

class ManageAuthoritiesTableViewModelSpec extends ViewTestHelper {

  "View model" should {

    "contain correct account id" in {
      val viewModelOb = ManageAuthoritiesTableViewModel(ACCOUNT_ID, CLOSED_CASH_ACC_WITH_AUTH_WITH_ID)

      viewModelOb.idString mustBe s"CdsCashAccount-$ACCOUNT_NUMBER"
    }

    "contain correct accountHeadingMsg" when {

      "account status is closed" in {
        val viewModelOb = ManageAuthoritiesTableViewModel(ACCOUNT_ID, CLOSED_CASH_ACC_WITH_AUTH_WITH_ID)

        viewModelOb.accountHeadingMsg mustBe messages(
          "manageAuthorities.table.heading.account.CdsCashAccount.closed", ACCOUNT_NUMBER)
      }

      "account status is pending" in {
        val viewModelOb = ManageAuthoritiesTableViewModel(ACCOUNT_ID, PENDING_CASH_ACC_WITH_AUTH_WITH_ID)

        viewModelOb.accountHeadingMsg mustBe messages(
          "manageAuthorities.table.heading.account.CdsCashAccount.pending", ACCOUNT_NUMBER)
      }

      "account status is suspended" in {
        val viewModelOb = ManageAuthoritiesTableViewModel(ACCOUNT_ID, SUSPENDED_CASH_ACC_WITH_AUTH_WITH_ID)

        viewModelOb.accountHeadingMsg mustBe messages(
          "manageAuthorities.table.heading.account.CdsCashAccount.suspended", ACCOUNT_NUMBER)
      }

      "account is of Northern Ireland and account type is Duty Deferment" in {
        val viewModelOb =
          ManageAuthoritiesTableViewModel(ACCOUNT_ID, OPEN_DD_ACC_WITH_AUTH_WITH_ID, isNiAccount = true)

        viewModelOb.accountHeadingMsg mustBe messages(
          "manageAuthorities.table.heading.account.CdsDutyDefermentAccount.Ni", ACCOUNT_NUMBER)
      }

      "account is non Northern Ireland and account type is Duty Deferment" in {
        val viewModelOb =
          ManageAuthoritiesTableViewModel(ACCOUNT_ID, OPEN_DD_ACC_WITH_AUTH_WITH_ID)

        viewModelOb.accountHeadingMsg mustBe messages(
          "manageAuthorities.table.heading.account.CdsDutyDefermentAccount", ACCOUNT_NUMBER)
      }

      "account status is None" in {
        val viewModelOb =
          ManageAuthoritiesTableViewModel(ACCOUNT_ID, NONE_DD_ACC_WITH_AUTH_WITH_ID)

        viewModelOb.accountHeadingMsg mustBe messages(
          "manageAuthorities.table.heading.account.CdsDutyDefermentAccount", ACCOUNT_NUMBER)
      }

      "CdsCashAccount status is open" in {
        val viewModelOb =
          ManageAuthoritiesTableViewModel(ACCOUNT_ID, OPEN_CASH_ACC_WITH_AUTH_WITH_ID)

        viewModelOb.accountHeadingMsg mustBe messages(
          "manageAuthorities.table.heading.account.CdsCashAccount", ACCOUNT_NUMBER)
      }

      "CdsGeneralGuaranteeAccount status is open" in {
        val viewModelOb =
          ManageAuthoritiesTableViewModel(ACCOUNT_ID, OPEN_GG_ACC_WITH_AUTH_WITH_ID)

        viewModelOb.accountHeadingMsg mustBe messages(
          "manageAuthorities.table.heading.account.CdsGeneralGuaranteeAccount", ACCOUNT_NUMBER)
      }
    }

    "contain correct authority header row" when {

      "CdsGeneralGuaranteeAccount status is open" in {
        val viewModelOb =
          ManageAuthoritiesTableViewModel(ACCOUNT_ID, OPEN_GG_ACC_WITH_AUTH_WITH_ID)

        val expectedRowViewModel: AuthorityHeaderRowViewModel = AuthorityHeaderRowViewModel(
          authCompanyId = s"account-authorities-table-user-heading-CdsGeneralGuaranteeAccount-$ACCOUNT_NUMBER",
          authCompanyHeaderValue = messages("manageAuthorities.table.heading.user"),
          startDateId = s"account-authorities-table-start-date-heading-CdsGeneralGuaranteeAccount-$ACCOUNT_NUMBER",
          startDateHeader = messages("manageAuthorities.table.heading.startDate"),
          endDateId = s"account-authorities-table-end-date-heading-CdsGeneralGuaranteeAccount-$ACCOUNT_NUMBER",
          endDateHeader = messages("manageAuthorities.table.heading.endDate"),
          viewBalanceId = s"account-authorities-table-view-balance-heading-CdsGeneralGuaranteeAccount-$ACCOUNT_NUMBER",
          viewBalanceHeaderValue = messages("manageAuthorities.table.heading.balance"),
          hiddenActionsId = s"account-authorities-table-actions-heading-CdsGeneralGuaranteeAccount-$ACCOUNT_NUMBER",
          hiddenActionsHeaderValue = messages("manageAuthorities.table.heading.actions")
        )

        viewModelOb.authHeaderRowViewModel mustBe expectedRowViewModel
      }
    }

    "contain correct authority rows" when {

      "account status is open" in {
        val viewModelOb =
          ManageAuthoritiesTableViewModel(ACCOUNT_ID, OPEN_GG_ACC_WITH_AUTH_WITH_ID)

        val expectedAuthRowsView: Seq[AuthorityRowViewModel] = Seq(
          AuthorityRowViewModel(
            authorisedEori =
              AuthorityRowColumnViewModel(messages("manageAuthorities.table.heading.user"), EORI_NUMBER),
            formattedFromDate =
              AuthorityRowColumnViewModel(
                messages("manageAuthorities.table.heading.startDate"),
                dateAsdMMMyyyy(START_DATE_1)
              ),
            formattedToDate =
              AuthorityRowColumnViewModel(messages("manageAuthorities.table.heading.endDate"), dateAsdMMMyyyy(END_DATE_1)),
            viewBalanceAsString =
              AuthorityRowColumnViewModel(
                messages("manageAuthorities.table.heading.balance"),
                messages("manageAuthorities.table.viewBalance.no")
              ),
            viewLink = AuthorityRowColumnViewModel(
              messages("manageAuthorities.table.view-or-change"),
              s"${messages("manageAuthorities.table.row.viewLink", EORI_NUMBER, ACCOUNT_NUMBER)} ${
                messages(s"manageAuthorities.table.heading.account.CdsGeneralGuaranteeAccount", ACCOUNT_NUMBER)
              }",
              href = Some(controllers.routes.ViewAuthorityController.onPageLoad(ACCOUNT_ID, AUTH_ID_B)),
              classValue = "govuk-table__cell view-or-change",
              spanClassValue = "govuk-visually-hidden")
          ),
          AuthorityRowViewModel(
            authorisedEori =
              AuthorityRowColumnViewModel(messages("manageAuthorities.table.heading.user"), EORI_NUMBER),
            formattedFromDate =
              AuthorityRowColumnViewModel(
                messages("manageAuthorities.table.heading.startDate"),
                dateAsdMMMyyyy(START_DATE_2)
              ),
            formattedToDate =
              AuthorityRowColumnViewModel(
                messages("manageAuthorities.table.heading.endDate"),
                dateAsdMMMyyyy(END_DATE_2)
              ),
            viewBalanceAsString =
              AuthorityRowColumnViewModel(
                messages("manageAuthorities.table.heading.balance"),
                messages("manageAuthorities.table.viewBalance.no")
              ),
            viewLink = AuthorityRowColumnViewModel(
              messages("manageAuthorities.table.view-or-change"),
              s"${messages("manageAuthorities.table.row.viewLink", EORI_NUMBER, ACCOUNT_NUMBER)} ${
                messages(s"manageAuthorities.table.heading.account.CdsGeneralGuaranteeAccount", ACCOUNT_NUMBER)
              }",
              href = Some(controllers.routes.ViewAuthorityController.onPageLoad(ACCOUNT_ID, AUTH_ID_C)),
              classValue = "govuk-table__cell view-or-change",
              spanClassValue = "govuk-visually-hidden")
          )
        )

        viewModelOb.authRows mustBe expectedAuthRowsView
      }

      "account status is closed" in {
        val viewModelOb =
          ManageAuthoritiesTableViewModel(ACCOUNT_ID, CLOSED_GG_ACC_WITH_AUTH_WITH_ID)

        val expectedAuthRowsView: Seq[AuthorityRowViewModel] = Seq(
          AuthorityRowViewModel(
            authorisedEori =
              AuthorityRowColumnViewModel(messages("manageAuthorities.table.heading.user"), EORI_NUMBER),
            formattedFromDate =
              AuthorityRowColumnViewModel(
                messages("manageAuthorities.table.heading.startDate"),
                dateAsdMMMyyyy(START_DATE_1)
              ),
            formattedToDate =
              AuthorityRowColumnViewModel(
                messages("manageAuthorities.table.heading.endDate"),
                dateAsdMMMyyyy(END_DATE_1)
              ),
            viewBalanceAsString =
              AuthorityRowColumnViewModel(
                messages("manageAuthorities.table.heading.balance"), messages("manageAuthorities.table.viewBalance.no")),
            viewLink = AuthorityRowColumnViewModel(
              messages("manageAuthorities.table.view-or-change"),
              s"${messages("manageAuthorities.table.row.viewLink", EORI_NUMBER, ACCOUNT_NUMBER)} ${
                messages(s"manageAuthorities.table.heading.account.CdsGeneralGuaranteeAccount", ACCOUNT_NUMBER)
              }",
              href = Some(controllers.routes.ViewAuthorityController.onPageLoad(ACCOUNT_ID, AUTH_ID_B)),
              isAccountStatusNonClosed = false,
              classValue = "govuk-table__cell view-or-change",
              spanClassValue = "govuk-visually-hidden")
          ),
          AuthorityRowViewModel(
            authorisedEori =
              AuthorityRowColumnViewModel(messages("manageAuthorities.table.heading.user"), EORI_NUMBER),
            formattedFromDate =
              AuthorityRowColumnViewModel(
                messages("manageAuthorities.table.heading.startDate"),
                dateAsdMMMyyyy(START_DATE_1)
              ),
            formattedToDate =
              AuthorityRowColumnViewModel(
                messages("manageAuthorities.table.heading.endDate"),
                dateAsdMMMyyyy(END_DATE_1)
              ),
            viewBalanceAsString =
              AuthorityRowColumnViewModel(
                messages("manageAuthorities.table.heading.balance"),
                messages("manageAuthorities.table.viewBalance.yes")
              ),
            viewLink = AuthorityRowColumnViewModel(
              messages("manageAuthorities.table.view-or-change"),
              s"${messages("manageAuthorities.table.row.viewLink", EORI_NUMBER, ACCOUNT_NUMBER)} ${
                messages(s"manageAuthorities.table.heading.account.CdsGeneralGuaranteeAccount", ACCOUNT_NUMBER)
              }",
              href = Some(controllers.routes.ViewAuthorityController.onPageLoad(ACCOUNT_ID, AUTH_ID_C)),
              isAccountStatusNonClosed = false,
              classValue = "govuk-table__cell view-or-change",
              spanClassValue = "govuk-visually-hidden")
          )
        )

        viewModelOb.authRows mustBe expectedAuthRowsView
      }
    }
  }

}
