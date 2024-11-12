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

package views.partials

import models.domain.{AccountStatusClosed, AccountWithAuthoritiesWithId}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import utils.TestData._
import utils.ViewTestHelper
import viewmodels.ManageAuthoritiesTableViewModel
import views.html.partials.ManageAuthoritiesTable

class ManageAuthoritiesTableSpec extends ViewTestHelper {

  "View" should {

    "display correct text and guidance" when {

      "account status is open" in {
        val authEoriAndCompanyMap = Map(EORI_NUMBER -> "test_company")

        val view = viewAsDoc(ACCOUNT_ID, OPEN_CASH_ACC_WITH_AUTH_WITH_ID, isNiAccount = false, authEoriAndCompanyMap)

        shouldContainCorrectAccountStatusMsgForStatusOpen(view, OPEN_CASH_ACC_WITH_AUTH_WITH_ID)
        shouldContainCorrectElementValuesForAuthorityRows(view, OPEN_CASH_ACC_WITH_AUTH_WITH_ID)
      }

      "account status is closed" in {
        val view = viewAsDoc(ACCOUNT_ID, CLOSED_CASH_ACC_WITH_AUTH_WITH_ID)

        shouldContainCorrectAccountStatusMsgForStatusClosed(view, CLOSED_CASH_ACC_WITH_AUTH_WITH_ID)
        shouldContainCorrectElementValuesForAuthorityRows(view, CLOSED_CASH_ACC_WITH_AUTH_WITH_ID)
      }

      "account status is suspended" in {
        val view = viewAsDoc(ACCOUNT_ID, SUSPENDED_CASH_ACC_WITH_AUTH_WITH_ID)

        shouldContainCorrectAccountStatusMsgForStatusSuspended(view, SUSPENDED_CASH_ACC_WITH_AUTH_WITH_ID)
        shouldContainCorrectElementValuesForAuthorityRows(view, SUSPENDED_CASH_ACC_WITH_AUTH_WITH_ID)
      }

      "account status is pending" in {
        val view = viewAsDoc(ACCOUNT_ID, PENDING_CASH_ACC_WITH_AUTH_WITH_ID)

        shouldContainCorrectAccountStatusMsgForStatusPending(view, PENDING_CASH_ACC_WITH_AUTH_WITH_ID)
        shouldContainCorrectElementValuesForAuthorityRows(view, PENDING_CASH_ACC_WITH_AUTH_WITH_ID)
      }

      "account status is None" in {
        val view = viewAsDoc(ACCOUNT_ID, NONE_DD_ACC_WITH_AUTH_WITH_ID)

        shouldContainCorrectAccountStatusMsgForStatusNone(view, NONE_DD_ACC_WITH_AUTH_WITH_ID)
        shouldContainCorrectElementValuesForAuthorityRows(view, NONE_DD_ACC_WITH_AUTH_WITH_ID)
      }

      "account is non Northern Ireland and account type is Duty Deferment" in {
        val view = viewAsDoc(ACCOUNT_ID, OPEN_DD_ACC_WITH_AUTH_WITH_ID)

        shouldContainCorrectAccountStatusMsgForDDNonNIAccount(view, OPEN_DD_ACC_WITH_AUTH_WITH_ID)
        shouldContainCorrectElementValuesForAuthorityRows(view, OPEN_DD_ACC_WITH_AUTH_WITH_ID)
      }
    }
  }

  private def viewAsDoc(accountId: String,
                        account: AccountWithAuthoritiesWithId,
                        isNiAccount: Boolean = false,
                        authEoriAndCompanyMap: Map[String, String] = Map.empty): Document =
    Jsoup.parse(app.injector.instanceOf[ManageAuthoritiesTable].apply(
      ManageAuthoritiesTableViewModel(accountId, account, isNiAccount, authEoriAndCompanyMap)
    ).body)

  private def shouldContainCorrectAccountStatusMsgForStatusOpen(view: Document,
                                                                account: AccountWithAuthoritiesWithId): Assertion =
    view.getElementById(s"${
      account.accountType
    }-${account.accountNumber}-heading").text() mustBe
      messages(s"manageAuthorities.table.heading.account.${account.accountType}", account.accountNumber)

  private def shouldContainCorrectAccountStatusMsgForStatusClosed(view: Document,
                                                                  account: AccountWithAuthoritiesWithId): Assertion =
    view.getElementById(s"${
      account.accountType
    }-${account.accountNumber}-heading").text() mustBe
      messages(s"manageAuthorities.table.heading.account.${account.accountType}.closed", account.accountNumber)

  private def shouldContainCorrectAccountStatusMsgForStatusSuspended(view: Document,
                                                                     account: AccountWithAuthoritiesWithId): Assertion =
    view.getElementById(s"${
      account.accountType
    }-${account.accountNumber}-heading").text() mustBe
      messages(s"manageAuthorities.table.heading.account.${account.accountType}.suspended", account.accountNumber)

  private def shouldContainCorrectAccountStatusMsgForStatusPending(view: Document,
                                                                   account: AccountWithAuthoritiesWithId): Assertion =
    view.getElementById(s"${
      account.accountType
    }-${account.accountNumber}-heading").text() mustBe
      messages(s"manageAuthorities.table.heading.account.${account.accountType}.pending", account.accountNumber)

  private def shouldContainCorrectAccountStatusMsgForStatusNone(view: Document,
                                                                account: AccountWithAuthoritiesWithId): Assertion =
    view.getElementById(s"${
      account.accountType
    }-${account.accountNumber}-heading").text() mustBe
      messages(s"manageAuthorities.table.heading.account.${account.accountType}", account.accountNumber)

  private def shouldContainCorrectAccountStatusMsgForDDNonNIAccount(view: Document,
                                                                    account: AccountWithAuthoritiesWithId): Assertion =
    view.getElementById(s"${
      account.accountType
    }-${account.accountNumber}-heading").text() mustBe
      messages(s"manageAuthorities.table.heading.account.${account.accountType}", account.accountNumber)

  private def shouldContainCorrectElementValuesForAuthorityRows(view: Document,
                                                                account: AccountWithAuthoritiesWithId,
                                                                authEoriAndCompanyMap: Map[String, String] = Map.empty): Assertion = {

    val tableRowHtml = view.getElementsByClass("govuk-table__row").html()

    tableRowHtml.contains(messages("manageAuthorities.table.heading.user")) mustBe true

    if(authEoriAndCompanyMap.contains(EORI_NUMBER)) {
      tableRowHtml.contains(messages("manageAuthorities.table.heading.user")) mustBe true
    }

    if (account.accountStatus.fold(true)(status => status != AccountStatusClosed)) {
      tableRowHtml.contains(messages("manageAuthorities.table.view-or-change")) mustBe true

      view.getElementsByClass("govuk-table__cell view-or-change").html()
        .contains(controllers.routes.ViewAuthorityController.onPageLoad(ACCOUNT_ID, AUTH_ID_B).toString) mustBe true

      view.getElementsByClass("govuk-table__cell view-or-change").html()
        .contains(controllers.routes.ViewAuthorityController.onPageLoad(ACCOUNT_ID, AUTH_ID_C).toString) mustBe true
    } else {
      tableRowHtml.contains(messages("manageAuthorities.table.view-or-change")) mustBe false
    }

  }
}
