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

package services

import base.SpecBase
import models.domain
import models.domain.FileFormat.Csv
import models.domain.FileRole.StandingAuthority
import models.domain.{Metadata, MetadataItem, StandingAuthorityFile, StandingAuthorityMetadata}
import play.api.i18n.Messages
import play.api.test.Helpers
import utils.StringUtils.emptyString
import utils.TestData.{DAY_1, FILE_SIZE_DEFAULT, MONTH_6, YEAR_2022}

class SdesGatekeeperServiceSpec extends SpecBase {

  implicit val messages: Messages = Helpers.stubMessages()
  "SdesGatekeeperService" should {

    "create StandingAuthorityFile from FileInformation" in {
      val sdesGatekeeperService = new SdesGatekeeperService()

      val standingAuthorityFileMetadata = List(
        MetadataItem("PeriodStartYear", "2022"),
        MetadataItem("PeriodStartMonth", "6"),
        MetadataItem("PeriodStartDay", "1"),
        MetadataItem("FileType", "CSV"),
        MetadataItem("FileRole", "StandingAuthority")
      )

      val fileInformationForStandingAuthorityCSV = domain.FileInformation(
        "authorities-2022-06.csv",
        "https://some.sdes.domain?token=abc123",
        FILE_SIZE_DEFAULT,
        Metadata(standingAuthorityFileMetadata)
      )

      val expectedStandingAuthorityFile = StandingAuthorityFile(
        "authorities-2022-06.csv",
        "https://some.sdes.domain?token=abc123",
        FILE_SIZE_DEFAULT,
        StandingAuthorityMetadata(YEAR_2022, MONTH_6, DAY_1, Csv, StandingAuthority),
        emptyString)

      val standingAuthorityFile = sdesGatekeeperService.convertToStandingAuthoritiesFile(
        fileInformationForStandingAuthorityCSV)

      standingAuthorityFile must be(expectedStandingAuthorityFile)
    }
  }
}
