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
import play.api.libs.json.{JsValue, Json}
import utils.TestData.COMPANY_NAME

class CompanyNameSpec extends SpecBase {

  "CompanyName" should {
    "generate correct output for Json Reads" in new Setup {
      companyNameJson.as[CompanyName] mustBe companyName
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(companyName) mustBe companyNameJson
    }
  }

  "handle missing optional name field" in new Setup {
    jsonWithoutName.as[CompanyName] mustBe expectedJsonWithoutName
  }

  "handle null value for optional name field" in new Setup {
    jsonWithNullName.as[CompanyName] mustBe expectedJsonWithNullName
  }

  trait Setup {
    val companyName: CompanyName = CompanyName(Some(COMPANY_NAME), "Yes")
    val companyNameJson: JsValue = Json.parse(
      """
        |{
        |  "name":"test_company",
        |  "consent":"Yes"
        |}
        |""".stripMargin
    )

    val jsonWithoutName: JsValue             = Json.parse(
      """
        |{
        |  "consent": "Yes"
        |}
        |""".stripMargin
    )
    val expectedJsonWithoutName: CompanyName = CompanyName(None, "Yes")

    val jsonWithNullName: JsValue             = Json.parse(
      """
        |{
        |  "name": null,
        |  "consent": "Yes"
        |}
        |""".stripMargin
    )
    val expectedJsonWithNullName: CompanyName = CompanyName(None, "Yes")
  }
}
