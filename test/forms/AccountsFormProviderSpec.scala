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

import forms.behaviours.CheckboxFieldBehaviours
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.data.FormError

class AccountsFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new AccountsFormProvider()()

  ".value" must {

    val fieldName = "value"
    val requiredKey = "accounts.error.required"
    val validValues = List("account_0", "account_1")

    "bind a single valid value" in {
      val data = Map(
        s"$fieldName[0]" -> validValues.head
      )
      form.bind(data).get shouldEqual validValues.take(1)
    }

    "bind a multiple valid values" in {
      val data = Map(
        s"$fieldName[0]" -> validValues.head,
        s"$fieldName[1]" -> validValues(1)
      )
      form.bind(data).get shouldEqual validValues.take(2)
    }

    "fail to bind when the answer is invalid" in {
      val data = Map(
        s"$fieldName[0]" -> "invalid value"
      )
      form.bind(data).errors should contain(FormError(s"$fieldName", "accounts.error.required"))
    }

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )
  }
}
