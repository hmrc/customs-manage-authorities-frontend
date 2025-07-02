/*
 * Copyright 2025 HM Revenue & Customs
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

package forms.mappings

import base.SpecBase
import org.scalatestplus.play.PlaySpec
import play.api.data.Forms.*
import play.api.data.{FormError, Forms, Mapping}
import uk.gov.voa.play.form.Condition

class AdditionalConstraintsMappingSpec extends SpecBase {

  val fieldKey = "testField"

  def alwaysTrue: Condition  = _ => true
  def alwaysFalse: Condition = _ => false

  val isEven: Int => Boolean  = _ % 2 == 0
  val nonZero: Int => Boolean = _ != 0

  val oddNumber  = 3
  val evenNumber = 4

  val baseMapping: Mapping[Int] = number.withPrefix(fieldKey)

  "AdditionalConstraintsMapping" should {

    "bind successfully with no conditional constraint applied (condition false)" in {
      val mapping = AdditionalConstraintsMapping(
        baseMapping,
        Seq(ConditionalConstraint(alwaysFalse, "error.even", isEven))
      )

      mapping.bind(Map(fieldKey -> "3")) mustBe Right(oddNumber)
    }

    "fail when conditional constraint applies and fails" in {
      val mapping = AdditionalConstraintsMapping(
        baseMapping,
        Seq(ConditionalConstraint(alwaysTrue, "error.even", isEven))
      )

      mapping.bind(Map(fieldKey -> "3")) mustBe Left(Seq(FormError(fieldKey, "error.even")))
    }

    "bind successfully when conditional constraint applies and passes" in {
      val mapping = AdditionalConstraintsMapping(
        baseMapping,
        Seq(ConditionalConstraint(alwaysTrue, "error.even", isEven))
      )

      mapping.bind(Map(fieldKey -> "4")) mustBe Right(evenNumber)
    }

    "apply only the first matching condition from the list" in {
      val mapping = AdditionalConstraintsMapping(
        baseMapping,
        Seq(
          ConditionalConstraint(alwaysTrue, "error.even", isEven),
          ConditionalConstraint(alwaysTrue, "error.nonzero", nonZero)
        )
      )

      mapping.bind(Map(fieldKey -> "3")) mustBe Left(Seq(FormError(fieldKey, "error.even")))
    }

    "return unbinded value from base mapping" in {
      val mapping = AdditionalConstraintsMapping(
        baseMapping,
        Seq.empty
      )

      mapping.unbind(oddNumber) mustBe Map(fieldKey -> "3")
    }

    "validate through bind -> unbindAndValidate -> errors round trip" in {
      val mapping = AdditionalConstraintsMapping(
        baseMapping,
        Seq(ConditionalConstraint(alwaysTrue, "error.even", isEven))
      )

      val (_, errors) = mapping.unbindAndValidate(oddNumber)
      errors mustBe Seq(FormError(fieldKey, "error.even"))
    }

    "preserve prefix handling" in {
      val mapping = AdditionalConstraintsMapping(
        baseMapping,
        Seq.empty
      )

      val prefixed = mapping.withPrefix("prefix")
      prefixed.bind(Map("prefix.testField" -> "3")) mustBe Right(oddNumber)
    }
  }
}
