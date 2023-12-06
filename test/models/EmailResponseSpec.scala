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

import org.scalatest.WordSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsSuccess, Json}

class EmailResponseSpec extends WordSpec with Matchers {

  "EmailResponse JSON" should {
    val undeliverableInfo = UndeliverableInformation("Test Subject")
    val emailResponse = EmailResponse(Some("test@example.com"), Some("2023-12-01"), Some(undeliverableInfo))
    val expectedJson = Json.obj(
      "address" -> "test@example.com",
      "timestamp" -> "2023-12-01",
      "undeliverableInformation" -> Json.obj("subject" -> "Test Subject")
    )

    "serialize to JSON correctly" in {
      val generatedJson = Json.toJson(emailResponse)
      generatedJson shouldBe expectedJson
    }

    "deserialize from JSON correctly" in {
      val result = Json.fromJson[EmailResponse](expectedJson)
      result shouldBe a[JsSuccess[_]]
      result.get shouldBe emailResponse
    }

    "serialize and then deserialize correctly" in {
      val json = Json.toJson(emailResponse)
      val deserializedResult = Json.fromJson[EmailResponse](json).asOpt
      deserializedResult shouldBe Some(emailResponse)
    }
  }
}
