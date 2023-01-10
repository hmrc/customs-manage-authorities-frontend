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

package connectors

import base.SpecBase
import models.withNameToString

class PackageSpec extends SpecBase {

  ".PackageSpec" must {
    "MDG_ACK_REF_LENGTH is 32" in {
      val result = connectors.MDG_ACK_REF_LENGTH
      result.length() mustBe 2
      result mustBe 32
    }

    ".acknowledgmentRef Generate Random Digits returns correct length" in {
      val result = connectors.acknowledgmentRef(3)
      result.length() mustBe 3
    }

    ".generateStringOfRandomDigits Generate Random Digits returns correct length" in {
      val result = connectors.generateStringOfRandomDigits(3)
      result.length() mustBe 3
    }
  }
}
