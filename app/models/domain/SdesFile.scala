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

import play.api.libs.json._
import play.api.{Logger, LoggerLike}

import java.time.LocalDate
import scala.collection.immutable.SortedSet

sealed abstract class FileFormat(val name: String) extends Ordered[FileFormat] {
  val order: Int

  def compare(that: FileFormat): Int = order.compare(that.order)

  override def toString: String = name
}

object FileFormat {

  case object Pdf extends FileFormat("PDF") {
    val order = 1
  }

  case object Csv extends FileFormat("CSV") {
    val order = 2
  }

  case object UnknownFileFormat extends FileFormat("UNKNOWN FILE FORMAT") {
    val order = 99
  }

  val log: LoggerLike = Logger(this.getClass)
  val authorityFileFormats: SortedSet[FileFormat] = SortedSet(Csv)

  def filterFileFormats[T <: SdesFile](
                                        allowedFileFormats: SortedSet[FileFormat])(
                                        files: Seq[T]): Seq[T] = files.filter(
    file => allowedFileFormats(file.metadata.fileFormat))

  def apply(name: String): FileFormat = name.toUpperCase match {
    case Pdf.name => Pdf
    case Csv.name => Csv
    case _ =>
      log.warn(s"Unknown file format: $name")
      UnknownFileFormat
  }

  implicit val fileFormatFormat: Format[FileFormat] = new Format[FileFormat] {
    def reads(json: JsValue): JsSuccess[FileFormat] = JsSuccess(apply(json.as[String]))

    def writes(obj: FileFormat): JsString = JsString(obj.name)
  }
}

sealed abstract class FileRole(val name: String,
                               val featureName: String,
                               val transactionName: String,
                               val messageKey: String)

object FileRole {
  case object StandingAuthority extends FileRole(
    "StandingAuthority", "authorities",
    "Display standing authorities csv", "authorities")

  val log: LoggerLike = Logger(this.getClass)

  def apply(name: String): FileRole = name match {
    case "StandingAuthority" => StandingAuthority
    case _ => throw new Exception(s"Unknown file role: $name")
  }
}

trait SdesFileMetadata {
  this: Product =>

  def fileFormat: FileFormat

  def fileRole: FileRole

  def periodStartYear: Int

  def periodStartMonth: Int

  def toMap[T <: SdesFileMetadata with Product]: Map[String, String] = {
    val fieldNames: Seq[String] = getClass.getDeclaredFields.toIndexedSeq.map(_.getName)
    val fieldValues: Seq[String] = productIterator.map(_.toString).toSeq
    fieldNames.zip(fieldValues).toMap
  }
}

trait SdesFile {
  def metadata: SdesFileMetadata

  def downloadURL: String

  def auditModelFor(eori: EORI): AuditModel = {
    val downloadStatementAuditData = DownloadStatementAuditData.apply(metadata, eori)
    val data = downloadStatementAuditData.auditData
    val auditModel = AuditModel("DisplayStandingAuthoritiesCSV", metadata.fileRole.transactionName, Json.toJson(data))
    auditModel
  }
}

case class StandingAuthorityMetadata(periodStartYear: Int,
                                     periodStartMonth: Int,
                                     periodStartDay: Int,
                                     fileFormat: FileFormat,
                                     fileRole: FileRole) extends SdesFileMetadata

case class StandingAuthorityFile(filename: String,
                                 downloadURL: String,
                                 size: Long,
                                 metadata: StandingAuthorityMetadata,
                                 eori: String) extends Ordered[StandingAuthorityFile] with SdesFile {

  val startDate: LocalDate = LocalDate.of(metadata.periodStartYear, metadata.periodStartMonth, metadata.periodStartDay)

  def compare(that: StandingAuthorityFile): Int = startDate.compareTo(that.startDate)
}
