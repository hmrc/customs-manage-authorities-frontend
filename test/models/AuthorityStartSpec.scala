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

package models

import base.SpecBase
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}

class AuthorityStartSpec extends SpecBase
  with ScalaCheckPropertyChecks
  with OptionValues {

  "AuthorityStart" must {

    "deserialise valid values" in {

      val gen = Gen.oneOf(AuthorityStart.values)

      forAll(gen) {
        authorityStart =>

          JsString(authorityStart.toString).validate[AuthorityStart].asOpt.value mustEqual authorityStart
      }
    }

    "fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!AuthorityStart.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[AuthorityStart] mustEqual JsError("error.invalid")
      }
    }

    "serialise" in {

      val gen = Gen.oneOf(AuthorityStart.values)

      forAll(gen) {
        authorityStart =>

          Json.toJson(authorityStart)(AuthorityStart.writes) mustEqual JsString(authorityStart.toString)
      }
    }
  }
}
