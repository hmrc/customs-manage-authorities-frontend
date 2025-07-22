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

package pages

import base.SpecBase
import play.api.libs.json.{JsValue, Json}

class ConfirmationPageSpec extends SpecBase {

  "ConfirmationDetails" should {
    "generate correct output for Json Reads" in new Setup {
      confirmationDetailsJson.as[ConfirmationDetails] mustBe confirmationDetails
    }

    "generate correct output for JSon Writes" in new Setup {
      Json.toJson(confirmationDetails) mustBe confirmationDetailsJson
    }
  }

  trait Setup {
    val confirmationDetails: ConfirmationDetails = ConfirmationDetails(
      "test_eori",
      Some("test_date"),
      Some("test_name"),
      false
    )

    val confirmationDetailsJson: JsValue = Json.parse(
      """
        |{
        | "eori": "test_eori",
        | "startDate": "test_date",
        | "companyName": "test_name",
        | "multipleAccounts": false
        |}""".stripMargin
    )
  }
}
