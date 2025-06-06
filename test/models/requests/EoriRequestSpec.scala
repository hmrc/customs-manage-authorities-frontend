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

package models.requests

import base.SpecBase
import play.api.libs.json.{JsSuccess, Json}

class EoriRequestSpec extends SpecBase {
  "EoriRequest" should {
    "return correct value for Json Reads" in new Setup {
      import models.requests.EoriRequest.format
      Json.fromJson(Json.parse(eoriRequestJsString)) mustBe JsSuccess(request)
    }

    "return correct value for Json Writes" in new Setup {
      Json.toJson(request) mustBe Json.parse(eoriRequestJsString)
    }
  }

  trait Setup {
    val request: EoriRequest = EoriRequest("someEori")
    val eoriRequestJsString  = """{"eori":"someEori"}"""
  }
}
