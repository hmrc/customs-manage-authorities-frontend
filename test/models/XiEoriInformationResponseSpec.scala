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
import play.api.libs.json.{JsResultException, JsValue, Json}

class XiEoriInformationResponseSpec extends SpecBase {

  "generate correct output for Json Reads" in new Setup {
    xiEoriInformationResponseJson.as[XiEoriInformationResponse] mustBe xiEoriInformationResponse
  }

  "generate correct output for Json Writes" in new Setup {
    Json.toJson(xiEoriInformationResponse) mustBe xiEoriInformationResponseJson
  }

  "handle missing optional address fields correctly when reading JSON" in new Setup {
    minimalJson.as[XiEoriInformationResponse] mustBe expectedMinimalJson
  }

  "fail to parse when required field is missing" in new Setup {
    intercept[JsResultException] {
      invalidJson.as[XiEoriInformationResponse]
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

    val xiEoriInformationResponse: XiEoriInformationResponse =
      XiEoriInformationResponse(xiEori = "XI12345", consent = "Yes", address = xiEoriAddressInformation)

    val xiEoriInformationResponseJson: JsValue = Json.parse(
      """
        |{
        |  "xiEori":"XI12345",
        |  "consent":"Yes",
        |  "address":
        |  {
        |    "pbeAddressLine1":"test_house",
        |    "pbeAddressLine2":"test_street",
        |    "pbeAddressLine3":"test_street",
        |    "pbeAddressLine4":"test_town",
        |    "pbePostCode":"Sw17 test"
        |  }
        |}
        |""".stripMargin
    )

    val minimalJson: JsValue = Json.parse(
      """
        |{
        |  "xiEori":"XI12345",
        |  "consent":"Yes",
        |  "address": {
        |    "pbeAddressLine1": "test_house"
        |  }
        |}
        |""".stripMargin
    )

    val expectedMinimalJson: XiEoriInformationResponse = XiEoriInformationResponse(
      xiEori = "XI12345",
      consent = "Yes",
      address = XiEoriAddressInformation(
        pbeAddressLine1 = "test_house",
        pbeAddressLine2 = None,
        pbeAddressLine3 = None,
        pbeAddressLine4 = None,
        pbePostCode = None
      )
    )

    val invalidJson: JsValue = Json.parse(
      """
        |{
        |  "consent": "Yes",
        |  "address": {
        |    "pbeAddressLine1": "test_house"
        |  }
        |}
        |""".stripMargin
    )
  }
}
