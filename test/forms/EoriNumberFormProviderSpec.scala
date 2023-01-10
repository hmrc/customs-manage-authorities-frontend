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

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Constraints
import org.scalacheck.Gen.choose
import play.api.data.FormError

class EoriNumberFormProviderSpec extends StringFieldBehaviours with Constraints{

  val requiredKey = "eoriNumber.error.required"
  val lengthKey = "eoriNumber.error.length"

  val validGenerator = for {
    digits <- choose[Long](100000000000L, 999999999999L)
  } yield {
    s"GB$digits"
  }

  val form = new EoriNumberFormProvider()()
  ".value" must {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validGenerator
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
