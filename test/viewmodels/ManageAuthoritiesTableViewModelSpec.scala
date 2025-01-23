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

import utils.TestData._
import utils.ViewTestHelper

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
          "manageAuthorities.table.heading.account.CdsCashAccount.closed",
          ACCOUNT_NUMBER
        )
      }

      "account status is pending" in {
        val viewModelOb = ManageAuthoritiesTableViewModel(ACCOUNT_ID, PENDING_CASH_ACC_WITH_AUTH_WITH_ID)

        viewModelOb.accountHeadingMsg mustBe messages(
          "manageAuthorities.table.heading.account.CdsCashAccount.pending",
          ACCOUNT_NUMBER
        )
      }

      "account status is suspended" in {
        val viewModelOb = ManageAuthoritiesTableViewModel(ACCOUNT_ID, SUSPENDED_CASH_ACC_WITH_AUTH_WITH_ID)

        viewModelOb.accountHeadingMsg mustBe messages(
          "manageAuthorities.table.heading.account.CdsCashAccount.suspended",
          ACCOUNT_NUMBER
        )
      }

      "account is of Northern Ireland and account type is Duty Deferment" in {
        val viewModelOb = ManageAuthoritiesTableViewModel(ACCOUNT_ID, OPEN_DD_ACC_WITH_AUTH_WITH_ID, isNiAccount = true)

        viewModelOb.accountHeadingMsg mustBe messages(
          "manageAuthorities.table.heading.account.CdsDutyDefermentAccount.Ni",
          ACCOUNT_NUMBER
        )
      }

      "account is non Northern Ireland and account type is Duty Deferment" in {
        val viewModelOb = ManageAuthoritiesTableViewModel(ACCOUNT_ID, OPEN_DD_ACC_WITH_AUTH_WITH_ID)

        viewModelOb.accountHeadingMsg mustBe messages(
          "manageAuthorities.table.heading.account.CdsDutyDefermentAccount",
          ACCOUNT_NUMBER
        )
      }

      "account status is None" in {
        val viewModelOb = ManageAuthoritiesTableViewModel(ACCOUNT_ID, NONE_DD_ACC_WITH_AUTH_WITH_ID)

        viewModelOb.accountHeadingMsg mustBe messages(
          "manageAuthorities.table.heading.account.CdsDutyDefermentAccount",
          ACCOUNT_NUMBER
        )
      }

      "CdsCashAccount status is open" in {
        val viewModelOb = ManageAuthoritiesTableViewModel(ACCOUNT_ID, OPEN_CASH_ACC_WITH_AUTH_WITH_ID)

        viewModelOb.accountHeadingMsg mustBe messages(
          "manageAuthorities.table.heading.account.CdsCashAccount",
          ACCOUNT_NUMBER
        )
      }

      "CdsGeneralGuaranteeAccount status is open" in {
        val viewModelOb = ManageAuthoritiesTableViewModel(ACCOUNT_ID, OPEN_GG_ACC_WITH_AUTH_WITH_ID)

        viewModelOb.accountHeadingMsg mustBe messages(
          "manageAuthorities.table.heading.account.CdsGeneralGuaranteeAccount",
          ACCOUNT_NUMBER
        )
      }
    }

    "contain correct authority rows" when {

      "account status is open" in {
        val viewModelOb = ManageAuthoritiesTableViewModel(ACCOUNT_ID, OPEN_GG_ACC_WITH_AUTH_WITH_ID)

        val expectedAuthRowsView: Seq[AuthorityRowViewModel] = authRowViewModelForOpenAccount

        viewModelOb.authRows mustBe expectedAuthRowsView
      }

      "account status is closed" in {
        val viewModelOb = ManageAuthoritiesTableViewModel(ACCOUNT_ID, CLOSED_GG_ACC_WITH_AUTH_WITH_ID)

        val expectedAuthRowsView: Seq[AuthorityRowViewModel] = authRowViewModelForClosedAccount()

        viewModelOb.authRows mustBe expectedAuthRowsView
      }

      "authorisedEoriAndCompanyMap in not empty" in {
        val authEorisAndCompanyMap = Map(EORI_NUMBER -> "test_company")
        val viewModelOb            = ManageAuthoritiesTableViewModel(
          ACCOUNT_ID,
          CLOSED_GG_ACC_WITH_AUTH_WITH_ID,
          isNiAccount = false,
          authEorisAndCompanyMap
        )

        val expectedAuthRowsView: Seq[AuthorityRowViewModel] = authRowViewModelForClosedAccount(authEorisAndCompanyMap)

        viewModelOb.authRows mustBe expectedAuthRowsView
      }
    }
  }

  private def authRowViewModelForOpenAccount: Seq[AuthorityRowViewModel] =
    Seq(
      AuthorityRowViewModel(
        authorisedEori = AuthorityRowColumnViewModel(messages("manageAuthorities.table.heading.user"), EORI_NUMBER),
        viewLink = AuthorityRowColumnViewModel(
          messages("manageAuthorities.table.view-or-change"),
          s"${messages("manageAuthorities.table.row.viewLink", EORI_NUMBER, ACCOUNT_NUMBER)} " +
            s"${messages(s"manageAuthorities.table.heading.account.CdsGeneralGuaranteeAccount", ACCOUNT_NUMBER)}",
          href = Some(controllers.routes.ViewAuthorityController.onPageLoad(ACCOUNT_ID, AUTH_ID_B)),
          classValue = "govuk-table__cell view-or-change",
          spanClassValue = "govuk-visually-hidden"
        )
      ),
      AuthorityRowViewModel(
        authorisedEori = AuthorityRowColumnViewModel(messages("manageAuthorities.table.heading.user"), EORI_NUMBER),
        viewLink = AuthorityRowColumnViewModel(
          messages("manageAuthorities.table.view-or-change"),
          s"${messages("manageAuthorities.table.row.viewLink", EORI_NUMBER, ACCOUNT_NUMBER)} " +
            s"${messages(s"manageAuthorities.table.heading.account.CdsGeneralGuaranteeAccount", ACCOUNT_NUMBER)}",
          href = Some(controllers.routes.ViewAuthorityController.onPageLoad(ACCOUNT_ID, AUTH_ID_C)),
          classValue = "govuk-table__cell view-or-change",
          spanClassValue = "govuk-visually-hidden"
        )
      )
    )

  private def authRowViewModelForClosedAccount(
    authEoriAndCompanyMap: Map[String, String] = Map.empty
  ): Seq[AuthorityRowViewModel] =
    Seq(
      AuthorityRowViewModel(
        authorisedEori = AuthorityRowColumnViewModel(messages("manageAuthorities.table.heading.user"), EORI_NUMBER),
        companyName = if (authEoriAndCompanyMap.contains(EORI_NUMBER)) {
          Some(
            AuthorityRowColumnViewModel(
              messages("manageAuthorities.table.heading.user"),
              authEoriAndCompanyMap(EORI_NUMBER)
            )
          )
        } else {
          None
        },
        viewLink = AuthorityRowColumnViewModel(
          messages("manageAuthorities.table.view-or-change"),
          s"${messages("manageAuthorities.table.row.viewLink", EORI_NUMBER, ACCOUNT_NUMBER)} " +
            s"${messages(s"manageAuthorities.table.heading.account.CdsGeneralGuaranteeAccount", ACCOUNT_NUMBER)}",
          href = Some(controllers.routes.ViewAuthorityController.onPageLoad(ACCOUNT_ID, AUTH_ID_B)),
          isAccountStatusNonClosed = false,
          classValue = "govuk-table__cell view-or-change",
          spanClassValue = "govuk-visually-hidden"
        )
      ),
      AuthorityRowViewModel(
        authorisedEori = AuthorityRowColumnViewModel(messages("manageAuthorities.table.heading.user"), EORI_NUMBER),
        companyName = if (authEoriAndCompanyMap.contains(EORI_NUMBER)) {
          Some(
            AuthorityRowColumnViewModel(
              messages("manageAuthorities.table.heading.user"),
              authEoriAndCompanyMap(EORI_NUMBER)
            )
          )
        } else {
          None
        },
        viewLink = AuthorityRowColumnViewModel(
          messages("manageAuthorities.table.view-or-change"),
          s"${messages("manageAuthorities.table.row.viewLink", EORI_NUMBER, ACCOUNT_NUMBER)} " +
            s"${messages(s"manageAuthorities.table.heading.account.CdsGeneralGuaranteeAccount", ACCOUNT_NUMBER)}",
          href = Some(controllers.routes.ViewAuthorityController.onPageLoad(ACCOUNT_ID, AUTH_ID_C)),
          isAccountStatusNonClosed = false,
          classValue = "govuk-table__cell view-or-change",
          spanClassValue = "govuk-visually-hidden"
        )
      )
    )
}
