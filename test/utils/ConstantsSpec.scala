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

package utils

import base.SpecBase
import utils.Constants.{MDG_ACK_REF_LENGTH, RANDOM_GENERATION_INT_LENGTH}

class ConstantsSpec extends SpecBase {

  "MDG_ACK_REF_LENGTH" should {

    "return correct value" in {
      MDG_ACK_REF_LENGTH mustBe 32
    }
  }

  "RANDOM_GENERATION_INT_LENGTH" should {

    "return correct value" in {
      RANDOM_GENERATION_INT_LENGTH mustBe 10
    }
  }

}
