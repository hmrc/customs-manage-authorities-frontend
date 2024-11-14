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

package config

import base.SpecBase
import config.Headers.{X_CLIENT_ID, X_SDES_KEY}

class HeadersSpec extends SpecBase {

  "X_CLIENT_ID" should {
    "return correct constant value" in {
      X_CLIENT_ID.trim mustBe "x-client-id"
    }
  }

  "X_SDES_KEY" should {
    "return correct constant value" in {
      X_SDES_KEY.trim mustBe "X-SDES-Key"
    }
  }
}
