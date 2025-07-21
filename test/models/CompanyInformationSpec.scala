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
import play.api.libs.json.{JsValue, Json}
import utils.StringUtils.emptyString

class CompanyInformationSpec extends SpecBase {

  ".GeneralInformationSpec" should {

    "Company Information must be valid" in new Setup {
      val result: CompanyInformation = CompanyInformation(name, consent, address)

      result mustBe corp
    }

    "Company Information must have a valid formatted address" in new Setup {
      val result: CompanyInformation = CompanyInformation(name, consent, address)

      result.formattedAddress mustBe formattedAddress
    }

    "generate correct output for Json Reads" in new Setup {
      corpJson.as[CompanyInformation] mustBe corp
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(corp) mustBe corpJson
    }

    "read CompanyInformation from JSON when optional postalCode is missing" in new Setup {
      val expectedCorp: CompanyInformation =
        CompanyInformation("TestCompany", "Yes", AddressInformation("123 Street", "New City", None, "GB"))

      corpJsonNoPostalCode.as[CompanyInformation] mustBe expectedCorp
    }
  }
}

trait Setup {
  val name: String    = "TestCompany"
  val consent: String = "Yes"

  val address: AddressInformation = AddressInformation("123 Street", "New City", Some("123 ABC"), "GB")

  val corp: CompanyInformation = CompanyInformation(name, consent, address)

  def formattedAddress: String = s"${address.streetAndNumber}, ${address.city}, " +
    s"${address.postalCode.getOrElse(emptyString)}, ${address.countryCode}"

  val corpJson: JsValue = Json.parse(s"""
       |{
       |  "name": "$name",
       |  "consent": "$consent",
       |  "address": {
       |    "streetAndNumber": "${address.streetAndNumber}",
       |    "city": "${address.city}",
       |    "postalCode": "${address.postalCode.get}",
       |    "countryCode": "${address.countryCode}"
       |  }
       |}
         """.stripMargin)

  val corpJsonNoPostalCode: JsValue = Json.parse(s"""
       |{
       |  "name": "$name",
       |  "consent": "$consent",
       |  "address": {
       |    "streetAndNumber": "${address.streetAndNumber}",
       |    "city": "${address.city}",
       |    "countryCode": "${address.countryCode}"
       |  }
       |}
           """.stripMargin)
}
