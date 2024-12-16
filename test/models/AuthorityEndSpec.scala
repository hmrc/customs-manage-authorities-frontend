/*
 * Copyright 2024 HM Revenue & Customs
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
import forms.AuthorityEndFormProvider
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.i18n.Messages
import play.api.libs.json.{JsError, JsString, Json}
import play.api.test.Helpers
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

class AuthorityEndSpec extends SpecBase with ScalaCheckPropertyChecks with OptionValues {
  "AuthorityEnd" must {
    "deserialize valid values" in {
      val gen = Gen.oneOf(AuthorityEnd.values)

      forAll(gen) { authorityEnd =>
        JsString(authorityEnd.toString).validate[AuthorityEnd].asOpt.value mustEqual authorityEnd
      }
    }

    "fail to deserialize invalid values" in {
      val gen = org.scalacheck.Arbitrary.arbitrary[String] suchThat (!AuthorityEnd.values.map(_.toString).contains(_))

      forAll(gen) { invalidValue =>
        JsString(invalidValue).validate[AuthorityEnd] mustEqual JsError("error.invalid")
      }
    }

    "serialise" in {
      val gen = Gen.oneOf(AuthorityEnd.values)

      forAll(gen) { authorityEnd =>
        Json.toJson(authorityEnd)(AuthorityEnd.writes) mustEqual JsString(authorityEnd.toString)
      }
    }

    "return correct values for options" in {
      implicit val msgs: Messages = Helpers.stubMessages()

      val radioItems = Seq(
        RadioItem(value = Some("indefinite"), content = Text(msgs("authorityEnd.indefinite")), checked = true),
        RadioItem(value = Some("setDate"), content = Text(msgs("authorityEnd.setDate")), checked = false)
      )

      val form = new AuthorityEndFormProvider().apply().fill(AuthorityEnd.Indefinite)

      AuthorityEnd.options(form)(msgs) shouldBe radioItems
    }
  }
}
