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

package models.domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._

class MetadataItemSpec extends AnyWordSpec with Matchers {

  "MetadataItem" should {

    "writes to JSON correctly" in {
      val metadataItem = MetadataItem("exampleKey", "exampleValue")

      val expectedJson = Json.obj(
        "key" -> "exampleKey",
        "value" -> "exampleValue"
      )

      Json.toJson(metadataItem) shouldEqual expectedJson
    }

    "reads from JSON correctly" in {
      val json = Json.obj(
        "metadata" -> "exampleKey",
        "value" -> "exampleValue"
      )

      val expectedMetadataItem = MetadataItem("exampleKey", "exampleValue")

      json.validate[MetadataItem] shouldEqual JsSuccess(expectedMetadataItem)
    }

    "Reads fail if 'metadata' field is missing" in {
      val json = Json.obj(
        "value" -> "exampleValue"
      )

      json.validate[MetadataItem] should matchPattern {
        case JsError(errors) if errors.exists(_._1.toString == "/metadata") =>
      }
    }

    "Reads fail if 'value' field is missing" in {
      val json = Json.obj(
        "metadata" -> "exampleKey"
      )

      json.validate[MetadataItem] should matchPattern {
        case JsError(errors) if errors.exists(_._1.toString == "/value") =>
      }
    }
  }
}
