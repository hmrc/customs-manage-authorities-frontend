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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._
import queries.{Gettable, Settable}

import java.time.{Instant, LocalDateTime, ZoneOffset}
import scala.util.{Failure, Success, Try}

final case class UserAnswers(id: String,
                             data: JsObject = Json.obj(),
                             lastUpdated: LocalDateTime = LocalDateTime.now) {

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(page.path)).reads(data).getOrElse(None)

  def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = {

    val updatedData = data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) => Success(jsValue)
      case JsError(ex) => Failure(new RuntimeException(ex.head._2.head.message))
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy (data = d)
        page.cleanup(Some(value), updatedAnswers)
    }
  }

  def remove[A](page: Settable[A]): Try[UserAnswers] = {

    val updatedData = data.setObject(page.path, JsNull) match {
      case JsSuccess(jsValue, _) => Success(jsValue)
      case JsError(ex) => Failure(new RuntimeException(ex.head._2.head.message))
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy (data = d)
        page.cleanup(None, updatedAnswers)
    }
  }
}

trait MongoJavatimeFormats {
  outer =>

  final val localDateTimeReads: Reads[LocalDateTime] =
    Reads.at[String](__ \ "$date" \ "$numberLong")
      .map(dateTime => Instant.ofEpochMilli(dateTime.toLong).atZone(ZoneOffset.UTC).toLocalDateTime)

  final val localDateTimeWrites: Writes[LocalDateTime] =
    Writes.at[String](__ \ "$date" \ "$numberLong")
      .contramap(_.toInstant(ZoneOffset.UTC).toEpochMilli.toString)

  final val localDateTimeFormat: Format[LocalDateTime] =
    Format(localDateTimeReads, localDateTimeWrites)

  trait Implicits {
    implicit val jatLocalDateTimeFormat: Format[LocalDateTime] = outer.localDateTimeFormat
  }
  object Implicits extends Implicits
}
object MongoJavatimeFormats extends MongoJavatimeFormats

object UserAnswers {

  implicit lazy val reads: Reads[UserAnswers] = {
    (
      (__ \ "_id").read[String] and
      (__ \ "data").read[JsObject] and
      (__ \ "lastUpdated").read(MongoJavatimeFormats.localDateTimeReads)
    ) (UserAnswers.apply _)
  }

  implicit lazy val writes: OWrites[UserAnswers] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "_id").write[String] and
      (__ \ "data").write[JsObject] and
      (__ \ "lastUpdated").write(MongoJavatimeFormats.localDateTimeWrites)
    ) (unlift(UserAnswers.unapply))
  }

  implicit val format: Format[UserAnswers] = Format(reads, writes)
}
