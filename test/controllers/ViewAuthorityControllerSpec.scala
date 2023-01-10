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

package controllers

import java.time.LocalDate

import base.SpecBase
import models.CompanyDetails
import models.domain._
import pages.add.{AccountsPage, AuthorityStartDatePage, EoriNumberPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._

class ViewAuthorityControllerSpec extends SpecBase {

  "View Authority Controller" must {
    ".onPageLoad calling any empty values gives you 404" in new Setup {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(
          GET, controllers.routes.ViewAuthorityController.onPageLoad(
            "", "").url)

        val result = route(application, request).value
        status(result) mustEqual NOT_FOUND
      }
    }
  }
}

trait Setup extends SpecBase {

  val startDate = LocalDate.now().plusMonths(1)

  val cashAccount = CashAccount(
    "12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))

  val dutyDeferment = DutyDefermentAccount(
    "67890", "GB210987654321", AccountStatusOpen, DutyDefermentBalance(None, None, None, None))

  val userAnswers = emptyUserAnswers
    .set(EoriNumberPage, CompanyDetails("GB123456789012", Some("Company Name"))).success.value
    .set(AuthorityStartDatePage, startDate).success.value
    .set(AccountsPage, List(cashAccount, dutyDeferment)).success.value
}
