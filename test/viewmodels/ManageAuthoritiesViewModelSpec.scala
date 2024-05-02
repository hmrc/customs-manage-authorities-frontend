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

import base.SpecBase
import models.domain._
import play.api.i18n.DefaultMessagesApi


import java.time.LocalDate

class ManageAuthoritiesViewModelSpec extends SpecBase {

  override val messagesApi = new DefaultMessagesApi(
    Map("en" ->
      Map("month.abbr.3" -> "Mar", "month.abbr.4" -> "Apr")
    )
  )

  val startDate: LocalDate = LocalDate.parse("2020-03-01")
  val endDate: LocalDate = LocalDate.parse("2020-04-01")

  val standingAuthorityWithView: StandingAuthority = StandingAuthority(
    "EORI1",
    LocalDate.parse("2020-03-01"),
    Some(LocalDate.parse("2020-04-01")),
    viewBalance = true
  )

  val standingAuthorityWithoutView: StandingAuthority = StandingAuthority(
    "EORI2",
    LocalDate.parse("2020-02-01"),
    None,
    viewBalance = false
  )

  val authorities: AuthoritiesWithId = AuthoritiesWithId(Map(
    "a" -> AccountWithAuthoritiesWithId(
      CdsDutyDefermentAccount, "1", Some(AccountStatusOpen), Map(
        "1" -> standingAuthorityWithView,
        "2" -> standingAuthorityWithoutView
      )),
    "b" -> AccountWithAuthoritiesWithId(
      CdsGeneralGuaranteeAccount, "2", Some(AccountStatusOpen), Map(
        "2" -> standingAuthorityWithView,
        "1" -> standingAuthorityWithoutView
      )),
    "c" -> AccountWithAuthoritiesWithId(
      CdsCashAccount, "4", Some(AccountStatusOpen), Map(
        "1" -> standingAuthorityWithoutView
      )),
    "d" -> AccountWithAuthoritiesWithId(
      CdsCashAccount, "3", Some(AccountStatusOpen), Map(
        "1" -> standingAuthorityWithoutView
      ))
  ))

  "ManageAuthoritiesViewModel" must {

    "return correct values for hasAccounts and hasNoAccounts" when {

      val cdsAccounts = CDSAccounts("GB123456789012", List(
        CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00))),
        CashAccount("23456", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(100.00)))
      ))

      "there is at least one authority" in {
        val viewModel = ManageAuthoritiesViewModel(authorities, cdsAccounts)

        viewModel.hasAccounts mustBe true
        viewModel.hasNoAccounts mustBe false
      }

      "there are no authorities" in {
        val viewModel = ManageAuthoritiesViewModel(AuthoritiesWithId(Seq.empty), cdsAccounts)

        viewModel.hasAccounts mustBe false
        viewModel.hasNoAccounts mustBe true
      }
    }

    "sort accounts by Cash, Duty deferment and General guarantee, then account number" in {
      val cdsAccounts = CDSAccounts("GB123456789012", List(
        CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00))),
        CashAccount("23456", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(100.00)))
      ))

      val viewModel = ManageAuthoritiesViewModel(authorities, cdsAccounts)
      viewModel.sortedAccounts.keys.toSeq mustBe Seq("d", "c", "a", "b")
    }

    "return correct value of authorisedEoriAndCompany map" in {
      val authEoriAndCompanyDetails = Map("test_eori_1" -> "company_1", "test_eori_2" -> "company_2")

      val cdsAccounts = CDSAccounts("GB123456789012", List(
        CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00))),
        CashAccount("23456", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(100.00)))
      ))

      val viewModel = ManageAuthoritiesViewModel(authorities, cdsAccounts, authEoriAndCompanyDetails)
      viewModel.auhorisedEoriAndCompanyMap mustBe authEoriAndCompanyDetails
    }
  }
}
