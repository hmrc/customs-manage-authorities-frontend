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

package forms.behaviours

import play.api.data.{Form, FormError}

trait DecimalFieldBehaviours extends FieldBehaviours {

  def decimalField(form: Form[_],
                   fieldName: String,
                   nonNumericError: FormError): Unit = {

    "not bind non-numeric numbers" in {

      forAll(nonNumerics -> "nonNumeric") {
        nonNumeric =>
          val result = form.bind(Map(fieldName -> nonNumeric)).apply(fieldName)
          result.errors shouldEqual Seq(nonNumericError)
      }
    }
  }

  def decimalFieldWithMinimum(form: Form[_],
                              fieldName: String,
                              minimum: Double,
                              expectedError: FormError): Unit = {

    s"not bind numbers below $minimum" in {

      forAll(decimalsBelowValue(minimum) -> "decimalBelowMin") {
        number: Double =>
          val result = form.bind(Map(fieldName -> number.toString)).apply(fieldName)
          result.errors shouldEqual Seq(expectedError)
      }
    }
  }

  def decimalFieldWithMaximum(form: Form[_],
                              fieldName: String,
                              maximum: Double,
                              expectedError: FormError): Unit = {

    s"not bind numbers above $maximum" in {

      forAll(decimalsAboveValue(maximum) -> "decimalAboveMax") {
        number: Double =>
          val result = form.bind(Map(fieldName -> number.toString)).apply(fieldName)
          result.errors shouldEqual Seq(expectedError)
      }
    }
  }

  def decimalsFieldWithRange(form: Form[_],
                             fieldName: String,
                             minimum: Double,
                             maximum: Double,
                             expectedError: FormError): Unit = {

    s"not bind numbers outside the range $minimum to $maximum" in {

      forAll(decimalsOutsideRange(minimum, maximum) -> "intOutsideRange") {
        number =>
          val result = form.bind(Map(fieldName -> number.toString)).apply(fieldName)
          result.errors shouldEqual Seq(expectedError)
      }
    }
  }
}