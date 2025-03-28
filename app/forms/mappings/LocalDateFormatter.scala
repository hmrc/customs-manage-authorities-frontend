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

package forms.mappings

import play.api.data.FormError
import play.api.data.format.Formatter

import java.time.LocalDate
import scala.util.{Failure, Success, Try}

private[mappings] class LocalDateFormatter(
  invalidKey: String,
  allRequiredKey: String,
  twoRequiredKey: String,
  requiredKey: String,
  args: Seq[String] = Seq.empty
) extends Formatter[LocalDate]
    with Formatters {

  private val fieldKeys: List[String] = List("day", "month", "year")
  private val maxValidDay             = 31
  private val maxValidMonth           = 12
  private val minAllowedYearValue     = 1000
  private val maxAllowedYearValue     = 99999

  private def toDate(key: String, day: Int, month: Int, year: Int): Either[Seq[FormError], LocalDate] =
    Try(LocalDate.of(year, month, day)) match {
      case Success(_) => Right(LocalDate.of(year, month, day))
      case Failure(_) => Left(Seq(FormError(updateFormErrorKeys(key, day, month, year), invalidKey, args)))
    }

  private def formatDate(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {

    val int = intFormatter(
      requiredKey = invalidKey,
      wholeNumberKey = invalidKey,
      nonNumericKey = invalidKey,
      args
    )

    for {
      day   <- int.bind(s"$key.day", data)
      month <- int.bind(s"$key.month", data)
      year  <- int.bind(s"$key.year", data)
      date  <- toDate(key, day, month, year)
    } yield date
  }

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {

    val fields = fieldKeys.map { field =>
      field -> data.get(s"$key.$field").filter(_.nonEmpty)
    }.toMap

    lazy val missingFields = fields
      .withFilter(_._2.isEmpty)
      .map(_._1)
      .toList

    fields.count(_._2.isDefined) match {
      case 3 =>
        formatDate(key, data).left.map {
          _.map(fe => fe.copy(key = fe.key, args = args))
        }

      case 2 =>
        Left(
          List(
            FormError(formErrorKeysInCaseOfEmptyOrNonNumericValues(key, data), requiredKey, missingFields ++ args)
          )
        )

      case 1 =>
        Left(
          List(
            FormError(formErrorKeysInCaseOfEmptyOrNonNumericValues(key, data), twoRequiredKey, missingFields ++ args)
          )
        )

      case _ =>
        Left(
          List(
            FormError(formErrorKeysInCaseOfEmptyOrNonNumericValues(key, data), allRequiredKey, args)
          )
        )
    }
  }

  override def unbind(key: String, value: LocalDate): Map[String, String] =
    Map(
      s"$key.day"   -> value.getDayOfMonth.toString,
      s"$key.month" -> value.getMonthValue.toString,
      s"$key.year"  -> value.getYear.toString
    )

  private[mappings] def updateFormErrorKeys(key: String, day: Int, month: Int, year: Int): String =
    (day, month, year) match {
      case (d, _, _) if d < 1 || d > maxValidDay                           => s"$key.day"
      case (_, m, _) if m < 1 || m > maxValidMonth                         => s"$key.month"
      case (_, _, y) if y < minAllowedYearValue || y > maxAllowedYearValue => s"$key.year"
      case _                                                               => s"$key.day"
    }

  private[mappings] def formErrorKeysInCaseOfEmptyOrNonNumericValues(key: String, data: Map[String, String]): String = {
    val dayValue   = data.get(s"$key.day")
    val monthValue = data.get(s"$key.month")
    val yearValue  = data.get(s"$key.year")

    (dayValue, monthValue, yearValue) match {
      case (Some(d), _, _) if d.trim.isEmpty || hasConversionToIntFailed(d) => s"$key.day"
      case (_, Some(m), _) if m.trim.isEmpty || hasConversionToIntFailed(m) => s"$key.month"
      case (_, _, Some(y)) if y.trim.isEmpty || hasConversionToIntFailed(y) => s"$key.year"
      case _                                                                => s"$key.day"
    }
  }

  private def hasConversionToIntFailed(strValue: String) =
    Try(strValue.trim.toInt).isFailure
}
