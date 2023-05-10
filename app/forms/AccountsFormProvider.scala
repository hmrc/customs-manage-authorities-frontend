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

package forms

import forms.mappings.Mappings
import models.domain.{AccountStatusPending, CDSAccount, DutyDefermentAccount}
import models.{AuthorisedAccounts, withNameToString}
import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import javax.inject.Inject

class AccountsFormProvider @Inject() extends Mappings {

  def apply(): Form[List[String]] =
    Form(
      "value" -> listText("accounts.error.required")
        .verifying(nonEmptyList("accounts.error.required"))
        .verifying("accounts.error.required", accounts => accounts.forall(_.startsWith("account_")))
    )
}

object AccountsFormProvider {

  def accountsHeadingKey(accounts: AuthorisedAccounts): String = {
    if(accounts.availableAccounts.length == 1) "accounts.heading.singleAccount" else "accounts.heading"
  }

  def accountsTitleKey(accounts: AuthorisedAccounts): String = {
    if(accounts.availableAccounts.length == 1) "accounts.title.singleAccount" else "accounts.title"
  }

  def options(form: Form[_], accounts: Seq[CDSAccount])(implicit messages: Messages): Seq[CheckboxItem] = accounts.zipWithIndex.map {
    case(account, index) =>
      val message = account match {
        case DutyDefermentAccount(_, _, status, _, _) if status == AccountStatusPending =>
          s"${messages("accounts.type." + account.accountType)}: ${account.number} ${messages("accounts.pending")}"
        case _ =>
          if (account.isNiAccount && account.) {
            s"${messages("accounts.type." + account.accountType)}: ${account.number} ${messages("accounts.ni")}"
          } else {
            s"${messages("accounts.type." + account.accountType)}: ${account.number}"
          }
      }

      CheckboxItem(
        name = Some(s"value[$index]"),
        id = Some(s"value${if (index > 0) index.toString else ""}"),
        value = s"account_${index.toString}",
        content = Text(message),
        checked = form.data.values.contains(s"account_${index.toString}")
      )
  }

}
