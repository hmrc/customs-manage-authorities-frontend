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
import utils.TestData._

class AuditModelSpec extends SpecBase {

  "apply" should {

    "correctly map StandingAuthorityFileMetadata and include the eori" in {
      val standingAuthorityFileMetadata = StandingAuthorityMetadata(
        YEAR_1972,
        MONTH_2,
        DAY_20,
        FileFormat.Csv,
        FileRole.StandingAuthority
      )

      val sut = DownloadStatementAuditData(standingAuthorityFileMetadata, "12345")
      sut.auditData mustBe Map(
        "eori"             -> "12345",
        "periodStartYear"  -> "1972",
        "periodStartMonth" -> "2",
        "periodStartDay"   -> "20",
        "fileFormat"       -> "CSV",
        "fileRole"         -> "StandingAuthority"
      )
    }
  }
}
