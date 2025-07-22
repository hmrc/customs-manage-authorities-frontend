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

package models

import base.SpecBase
import play.api.libs.json
import play.api.libs.json.{JsResultException, JsValue, Json}

class XiEoriAddressInformationSpec extends SpecBase {

  "XiEoriAddressInformation" should {
    "generate correct output for Json Reads" in new Setup {
      xiEoriAddressInformationJson.as[XiEoriAddressInformation] mustBe xiEoriAddressInformation
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(xiEoriAddressInformation) mustBe xiEoriAddressInformationJson
    }

    "handle missing optional fields correctly when reading JSON" in new Setup {
      jsonWithoutOptionals.as[XiEoriAddressInformation] mustBe expectedNoOptionsJson
    }

    "fail to parse when required field is missing" in new Setup {
      intercept[JsResultException] {
        invalidJson.as[XiEoriAddressInformation]
      }
    }
  }

  trait Setup {
    val xiEoriAddressInformation: XiEoriAddressInformation = XiEoriAddressInformation(
      pbeAddressLine1 = "test_house",
      pbeAddressLine2 = Some("test_street"),
      pbeAddressLine3 = Some("test_street"),
      pbeAddressLine4 = Some("test_town"),
      pbePostCode = Some("Sw17 test")
    )

    val xiEoriAddressInformationJson: JsValue = Json.parse(
      """
        |{
        |   "pbeAddressLine1":"test_house",
        |   "pbeAddressLine2":"test_street",
        |   "pbeAddressLine3":"test_street",
        |   "pbeAddressLine4":"test_town",
        |   "pbePostCode":"Sw17 test"
        |}
        |""".stripMargin
    )

    val invalidJson: JsValue = Json.parse(
      """
        |{
        |   "pbeAddressLine2": "test_street"
        |}
        |""".stripMargin
    )

    val jsonWithoutOptionals: JsValue = Json.parse(
      """
        |{
        |   "pbeAddressLine1": "test_house"
        |}
        |""".stripMargin
    )

    val expectedNoOptionsJson: XiEoriAddressInformation = XiEoriAddressInformation(
      pbeAddressLine1 = "test_house",
      pbeAddressLine2 = None,
      pbeAddressLine3 = None,
      pbeAddressLine4 = None,
      pbePostCode = None
    )
  }
}
