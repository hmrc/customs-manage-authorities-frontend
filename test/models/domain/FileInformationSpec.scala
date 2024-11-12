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
import play.api.libs.json.Json
import utils.TestData.FILE_SIZE_DEFAULT

class FileInformationSpec extends SpecBase {

  "FileInformation" should {

    "be able to read from json" in {

      val json =
        """  {
          |    "filename": "pvat-2018-06.csv",
          |    "downloadURL": "https://some.sdes.domain?token=abc123",
          |    "fileSize": 1234,
          |    "metadata": [
          |      { "metadata": "periodStartYear", "value": "2018" },
          |      { "metadata": "periodStartMonth", "value": "6" },
          |      { "metadata": "fileType", "value": "CSV" },
          |      { "metadata": "fileRole", "value": "pvat" }
          |    ]
          |  }""".stripMargin

      val expectedFileInformation = FileInformation(
        "pvat-2018-06.csv",
        "https://some.sdes.domain?token=abc123",
        FILE_SIZE_DEFAULT,
        Metadata(List(
          MetadataItem("periodStartYear", "2018"),
          MetadataItem("periodStartMonth", "6"),
          MetadataItem("fileType", "CSV"),
          MetadataItem("fileRole", "pvat")
        ))
      )

      val fileInformation = Json.parse(json).as[FileInformation]

      fileInformation must be(expectedFileInformation)

    }

    "be able to write to json" in {
      val fileInformation = FileInformation(
        "pvat-2018-06.csv",
        "https://some.sdes.domain?token=abc123",
        FILE_SIZE_DEFAULT,
        Metadata(List(
          MetadataItem("periodStartYear", "2018"),
          MetadataItem("periodStartMonth", "6"),
          MetadataItem("fileType", "CSV"),
          MetadataItem("fileRole", "pvat")
        ))
      )

      val expectedJson =
        """  {
          |    "filename": "pvat-2018-06.csv",
          |    "downloadURL": "https://some.sdes.domain?token=abc123",
          |    "fileSize": 1234,
          |    "metadata": [
          |      { "metadata": "periodStartYear", "value": "2018" },
          |      { "metadata": "periodStartMonth", "value": "6" },
          |      { "metadata": "fileType", "value": "CSV" },
          |      { "metadata": "fileRole", "value": "pvat" }
          |    ]
          |  }""".stripMargin

      val json = Json.toJson(fileInformation)

      json must be(Json.parse(expectedJson))
    }
  }
}
