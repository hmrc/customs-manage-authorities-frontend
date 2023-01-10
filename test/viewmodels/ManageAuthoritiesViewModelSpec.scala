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

  implicit val messages = messagesApi.preferred(fakeRequest())

  val startDate = LocalDate.parse("2020-03-01")
  val endDate = LocalDate.parse("2020-04-01")
  val standingAuthorityWithView = StandingAuthority(
    "EORI1",
    LocalDate.parse("2020-03-01"),
    Some(LocalDate.parse("2020-04-01")),
    viewBalance = true
  )
  val standingAuthorityWithoutView = StandingAuthority(
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

      "there is at least one authority" in {
        val viewModel = ManageAuthoritiesViewModel(authorities)

        viewModel.hasAccounts mustBe true
        viewModel.hasNoAccounts mustBe false
      }

      "there are no authorities" in {
        val viewModel = ManageAuthoritiesViewModel(AuthoritiesWithId(Seq.empty))

        viewModel.hasAccounts mustBe false
        viewModel.hasNoAccounts mustBe true
      }

    }

    "sort accounts by Cash, Duty deferment and General guarantee, then account number" in {
      val viewModel = ManageAuthoritiesViewModel(authorities)

      viewModel.sortedAccounts.keys.toSeq mustBe Seq("d", "c", "a", "b")
    }

    "sort authorities by start date" in {
      import viewmodels.ManageAuthoritiesViewModel.AccountWithAuthoritiesViewModel

      val viewModel = ManageAuthoritiesViewModel(authorities)

      viewModel.sortedAccounts.get("a").value.sortedAuthorities.keys.toSeq mustBe Seq("2", "1")
    }

    "assign id to authorities based on account type and account number" in {
      import viewmodels.ManageAuthoritiesViewModel.AccountWithAuthoritiesViewModel

      val viewModel = ManageAuthoritiesViewModel(authorities)

      viewModel.sortedAccounts.get("a").value.id mustBe "CdsDutyDefermentAccount-1"
      viewModel.sortedAccounts.get("b").value.id mustBe "CdsGeneralGuaranteeAccount-2"
      viewModel.sortedAccounts.get("c").value.id mustBe "CdsCashAccount-4"
      viewModel.sortedAccounts.get("d").value.id mustBe "CdsCashAccount-3"
    }

    "generate view balance message key for standing authority" when {

      "view balance is true" in {
        import viewmodels.ManageAuthoritiesViewModel.StandingAuthorityViewModel

        standingAuthorityWithView.viewBalanceAsString mustBe "manageAuthorities.table.viewBalance.yes"
      }

      "view balance is false" in {
        import viewmodels.ManageAuthoritiesViewModel.StandingAuthorityViewModel

        standingAuthorityWithoutView.viewBalanceAsString mustBe "manageAuthorities.table.viewBalance.no"
      }

    }

    "format authority start dates" in {
      import viewmodels.ManageAuthoritiesViewModel.StandingAuthorityViewModel

      standingAuthorityWithView.formattedFromDate mustBe "1 Mar 2020"
    }
    "format authority end dates" when {
      import viewmodels.ManageAuthoritiesViewModel.StandingAuthorityViewModel

      "when present" in {
        standingAuthorityWithView.formattedToDate mustBe Some("1 Apr 2020")
      }

      "when missing" in {
        standingAuthorityWithoutView.formattedToDate mustBe None
      }
    }
  }
}
