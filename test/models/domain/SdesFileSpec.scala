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
import models.domain.FileFormat.{Csv, Pdf, UnknownFileFormat}
import play.api.i18n.Messages
import play.api.libs.json.{JsString, JsSuccess, Json}
import play.api.test.Helpers.stubMessages
import utils.StringUtils.emptyString
import utils.TestData.{LENGTH_11, LENGTH_27, YEAR_2017, YEAR_2022, YEAR_2023}

class SdesFileSpec extends SpecBase {

  "an SdesFile" should {

    "be correctly ordered by start date" in new Setup {

      val file1: StandingAuthorityFile = standingAuthorityFile.copy(
        metadata = StandingAuthorityMetadata(YEAR_2022, month, day, Pdf, models.domain.FileRole.StandingAuthority)
      )

      val file2: StandingAuthorityFile = standingAuthorityFile.copy(
        metadata = StandingAuthorityMetadata(YEAR_2023, month, day, Csv, models.domain.FileRole.StandingAuthority)
      )

      List(file2, file1).sorted mustBe List(file1, file2)
    }
  }

  "FileRole" should {
    "return correct output for apply method" in new Setup {
      FileRole("StandingAuthority") mustBe models.domain.FileRole.StandingAuthority

      intercept[Exception] {
        FileRole("Unknown")
      }
    }

    "return correct output for unapply method" in new Setup {
      val fileRoleName: String = FileRole("StandingAuthority") match {
        case FileRole(name) => name
        case _ => emptyString
      }

      fileRoleName mustBe "StandingAuthority"
    }

    "return correct output for Reads" in new Setup {

      import models.domain.FileRole.fileRoleFormat

      Json.fromJson(JsString("StandingAuthority")) mustBe JsSuccess(FileRole("StandingAuthority"))
    }

    "return correct output for Writes" in new Setup {
      Json.toJson(FileRole("StandingAuthority")) mustBe JsString("StandingAuthority")
    }

    "bind correct value for pathBinder" in {
      import models.domain.FileRole.pathBinder

      pathBinder.bind(emptyString, "authorities") mustBe Right(models.domain.FileRole.StandingAuthority)
      pathBinder.bind(emptyString, "unknown") mustBe Left(s"unknown file role: unknown")
    }

    "unbind correct value for pathBinder" in {
      import models.domain.FileRole.pathBinder

      pathBinder.unbind(emptyString, models.domain.FileRole.StandingAuthority) mustBe "authorities"
    }
  }


  "FileFormat" should {
    "return correct value for apply method" in new Setup {
      FileFormat("PDF") mustBe Pdf
      FileFormat("CSV") mustBe Csv
      FileFormat("UNKNOWN FILE FORMAT") mustBe UnknownFileFormat
    }

    "return correct value for unapply method" in new Setup {
      val pdfFileFormatName: String = FileFormat("PDF") match {
        case FileFormat(name) => name
        case _ => emptyString
      }

      pdfFileFormatName mustBe "PDF"
    }
  }

  "StandingAuthorityFile" should {
    "sort the files correctly" in new Setup {
      val standAuthFile1: StandingAuthorityFile = StandingAuthorityFile(fileName,
        downloadUrl,
        size,
        standAuthMetaData,
        eori)

      val standAuthFile2: StandingAuthorityFile = StandingAuthorityFile(fileName,
        downloadUrl,
        size,
        standAuthMetaData.copy(periodStartYear = standAuthMetaData.periodStartYear + 1),
        eori)

      List(standAuthFile2, standAuthFile1).sorted mustBe List(standAuthFile1, standAuthFile2)
    }
  }

  "FileFormat" should {
    "generate correct output" when {
      "reads" in new Setup {

        import FileFormat.fileFormatFormat

        Json.fromJson(JsString("PDF")) mustBe JsSuccess(Pdf)
      }

      "writes" in new Setup {
        Json.toJson[FileFormat](Pdf) mustBe JsString(Pdf.name)
      }
    }
  }

  trait Setup {
    implicit val msg: Messages = stubMessages()

    val fileName = "test_file"
    val downloadUrl = "test_url"
    val size = 2064L

    val startYear = 2021
    val month = 10
    val day = 2
    val eori = "test_eori"

    val standAuthMetaData: StandingAuthorityMetadata =
      StandingAuthorityMetadata(startYear, month, day, Pdf, models.domain.FileRole.StandingAuthority)

    val standingAuthorityFile: StandingAuthorityFile = StandingAuthorityFile(
      "", "", size, StandingAuthorityMetadata(YEAR_2017, LENGTH_11, LENGTH_27, Pdf, models.domain.FileRole.StandingAuthority), eori)
  }

}
