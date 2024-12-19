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

import base.SpecBase
import play.api.libs.json.{JsObject, Json}

class MetadataSpec extends SpecBase {

  "Metadata" should {
    "serialize to JSON correctly" in {
      val metadataItem1 = MetadataItem("key1", "value1")
      val metadataItem2 = MetadataItem("key2", "value2")
      val metadata      = Metadata(Seq(metadataItem1, metadataItem2))

      val expectedJson = Json.arr(
        JsObject(Seq("metadata" -> Json.toJson("key1"), "value" -> Json.toJson("value1"))),
        JsObject(Seq("metadata" -> Json.toJson("key2"), "value" -> Json.toJson("value2")))
      )

      Json.toJson(metadata) mustEqual expectedJson
    }

    "deserialize from JSON correctly" in {
      val json = Json.arr(
        JsObject(Seq("metadata" -> Json.toJson("key1"), "value" -> Json.toJson("value1"))),
        JsObject(Seq("metadata" -> Json.toJson("key2"), "value" -> Json.toJson("value2")))
      )

      val metadataItem1    = MetadataItem("key1", "value1")
      val metadataItem2    = MetadataItem("key2", "value2")
      val expectedMetadata = Metadata(Seq(metadataItem1, metadataItem2))

      json.validate[Metadata].asOpt must be(Some(expectedMetadata))
    }
  }

  "asMap" should {

    "convert a sequence of MetadataItems into a map" in {
      val items = Seq(
        MetadataItem("key1", "value1"),
        MetadataItem("key2", "value2"),
        MetadataItem("key3", "value3")
      )

      val metadata = Metadata(items)

      val expectedMap = Map(
        "key1" -> "value1",
        "key2" -> "value2",
        "key3" -> "value3"
      )

      metadata.asMap mustBe expectedMap
    }

    "return an empty map when there are no items" in {
      val metadata = Metadata(Seq.empty)

      metadata.asMap mustBe empty
    }

    "only keep the last occurrence of duplicate keys" in {
      val items = Seq(
        MetadataItem("key1", "value1"),
        MetadataItem("key2", "value2"),
        MetadataItem("key1", "new_value1")
      )

      val metadata = Metadata(items)

      val expectedMap = Map(
        "key1" -> "new_value1",
        "key2" -> "value2"
      )

      metadata.asMap mustBe expectedMap
    }
  }
}
