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
  }
}

trait Setup {
  val name: String = "TestCompany"
  val consent: String = "Yes"

  val address: AddressInformation = AddressInformation(
    "123 Street", "New City", Some("123 ABC"), "GB")

  val corp = CompanyInformation(name, consent, address)

  def formattedAddress: String = s"${address.streetAndNumber}, ${address.city}, " +
    s"${address.postalCode.getOrElse(emptyString)}, ${address.countryCode}"
}
