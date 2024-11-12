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

package services

import models.domain.{FileFormat, FileInformation, FileRole, SdesFile, StandingAuthorityFile, StandingAuthorityMetadata}
import utils.StringUtils.emptyString

import javax.inject.Singleton

@Singleton
class SdesGatekeeperService() {

  implicit def convertToStandingAuthoritiesFile(sdesResponseFile: FileInformation): StandingAuthorityFile = {
    val metadata = sdesResponseFile.metadata.asMap

    StandingAuthorityFile(
      sdesResponseFile.filename,
      sdesResponseFile.downloadURL,
      sdesResponseFile.fileSize,
      StandingAuthorityMetadata(
        metadata("PeriodStartYear").toInt,
        metadata("PeriodStartMonth").toInt,
        metadata("PeriodStartDay").toInt,
        FileFormat(metadata("FileType")),
        FileRole(metadata("FileRole"))),
      emptyString
    )
  }

  def convertTo[T <: SdesFile](implicit converter: FileInformation => T): Seq[FileInformation] => Seq[T] = _.map(converter)
}
