/*
 * Copyright 2022 HM Revenue & Customs
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

package forms

import base.SpecBase
import models.domain.{AccountStatusOpen, CDSCashBalance, CashAccount}
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem

class AccountsFormProviderObjectSpec extends SpecBase {

  val form = new AccountsFormProvider()()

  ".options" must {

    "create correct checkboxes for view" in {

      val accounts = Seq(
        CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00))),
        CashAccount("54321", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))
      )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val result = AccountsFormProvider.options(form, accounts)(messages(application))

        result mustEqual Seq(
          CheckboxItem(
            name = Some("value[0]"),
            id = Some("value"),
            value = "account_0",
            content = Text(messages(application)("accounts.type.cash") + ": 12345"),
            checked = false
          ),
          CheckboxItem(
            name = Some("value[1]"),
            id = Some("value1"),
            value = "account_1",
            content = Text(messages(application)("accounts.type.cash") + ": 54321"),
            checked = false
          )
        )
      }
    }

  }

}
