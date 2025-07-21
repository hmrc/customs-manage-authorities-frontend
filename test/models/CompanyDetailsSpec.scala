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
import play.api.libs.json.{Format, JsObject, JsResult, JsValue, Json, OFormat, Reads, Writes}
import utils.TestData.*

class CompanyDetailsSpec extends SpecBase {

  "CompanyDetails" should {

    "serialize and deserialize using companyDetailsFormat" in new Setup {
      val format: Format[CompanyDetails] = CompanyDetails.companyDetailsFormat
      val json: JsValue                  = format.writes(companyDetails)
      json mustBe companyDetailsJson
      format.reads(json).get mustBe companyDetails
    }

    "serialize and deserialize using companyDetailsOFormat" in new Setup {
      val format: OFormat[CompanyDetails] = CompanyDetails.companyDetailsOFormat
      val json: JsObject                  = format.writes(companyDetails)
      json mustBe companyDetailsJson
      format.reads(json).get mustBe companyDetails
    }

    "deserialize using companyDetailsReads" in new Setup {
      private val reads                    = CompanyDetails.companyDetailsReads
      val result: JsResult[CompanyDetails] = reads.reads(companyDetailsJson)
      result.get mustBe companyDetails
    }

    "serialize using companyDetailsWrites" in new Setup {
      private val writes = CompanyDetails.companyDetailsWrites
      val json: JsValue  = writes.writes(companyDetails)
      json mustBe companyDetailsJson
    }
  }

  trait Setup {
    val writes: Writes[CompanyDetails] = implicitly[OFormat[CompanyDetails]]
    val reads: Reads[CompanyDetails]   = implicitly[OFormat[CompanyDetails]]

    val companyDetails: CompanyDetails = CompanyDetails(eori = EORI_NUMBER, name = Some(COMPANY_NAME))

    val companyDetailsJson: JsValue = Json.parse(
      """
        | {
        |   "eori":"EORI",
        |   "name":"test_company"
        | }
        |""".stripMargin
    )
  }
}
