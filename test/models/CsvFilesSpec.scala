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

package models

import base.SpecBase
import domain.FileFormat.Csv
import domain.FileRole.StandingAuthority
import domain.{StandingAuthorityFile, StandingAuthorityMetadata}
import models.CsvFiles.{partitionAsXiAndGb, xiCsvFileNameRegEx}
import utils.StringUtils.emptyString

class CsvFilesSpec extends SpecBase {

  "xiCsvFileNameRegEx" should {
    "return true when string matches the regex" in {
      "SA_XI_000000000154_csv.csv".matches(xiCsvFileNameRegEx) mustBe true
      "SA_XI_00000005666666y153_csv.csv".matches(xiCsvFileNameRegEx) mustBe true
      "SA_XI_avbncgg_csv.csv".matches(xiCsvFileNameRegEx) mustBe true
    }

    "return false when string does not match the regex" in {
      "SA_000000000153_csv.csv".matches(xiCsvFileNameRegEx) mustBe false
      "authorities-2022-11.csv".matches(xiCsvFileNameRegEx) mustBe false
      "TA_000000000154_XI_csv.csv".matches(xiCsvFileNameRegEx) mustBe false
      "SA_000000000156_csv.csv".matches(xiCsvFileNameRegEx) mustBe false
      "_000000000156_csv.csv".matches(xiCsvFileNameRegEx) mustBe false
      "000000000156_csv.csv".matches(xiCsvFileNameRegEx) mustBe false
      "SA_000000000156.csv".matches(xiCsvFileNameRegEx) mustBe false
    }
  }

  "partitionAsXiAndGb" should {
    "return correct list of GB and XI authorities partitioned by the file name pattern" in new Setup {
      val standAuthMetadata: StandingAuthorityMetadata =
        StandingAuthorityMetadata(year, month, day, Csv, StandingAuthority)

      val gbAuthFiles: Seq[StandingAuthorityFile] = Seq(
        StandingAuthorityFile("SA_000000000153_csv.csv", emptyString, size, standAuthMetadata, "GB123456789012"),
        StandingAuthorityFile("SA_000000000154_csv.csv", emptyString, size, standAuthMetadata, "GB123456789012")
      )

      val xiAuthFiles: Seq[StandingAuthorityFile] =
        Seq(
          StandingAuthorityFile("SA_XI_000000000153_csv.csv", emptyString, size, standAuthMetadata, "XI123456789012"),
          StandingAuthorityFile("SA_XI_000000000154_csv.csv", emptyString, size, standAuthMetadata, "XI123456789012")
        )

      val csvFileForBothGBAndXI: Seq[StandingAuthorityFile] = gbAuthFiles ++ xiAuthFiles

      partitionAsXiAndGb(csvFileForBothGBAndXI) mustBe
        CsvFiles(gbCsvFiles = gbAuthFiles, xiCsvFiles = xiAuthFiles)

      partitionAsXiAndGb(gbAuthFiles) mustBe CsvFiles(gbCsvFiles = gbAuthFiles, xiCsvFiles = Seq.empty)

      partitionAsXiAndGb(xiAuthFiles) mustBe CsvFiles(gbCsvFiles = Seq.empty, xiCsvFiles = xiAuthFiles)
    }

    "return empty list of GB and XI authorities partitioned when input list is empty" in {
      partitionAsXiAndGb(Seq.empty) mustBe CsvFiles(Seq.empty, Seq.empty)
    }
  }

  trait Setup {
    val seven: String = "1234567"
    val nine: String  = "123456789"

    val year: Int  = 2022
    val month: Int = 6
    val day: Int   = 1

    val size = 500L
  }
}
