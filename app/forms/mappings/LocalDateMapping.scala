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

import forms.mappings.Validation.{isEmptyOr, isInRange, nonEmpty}
import play.api.data.Forms.text
import play.api.data.{Forms, Mapping}
import uk.gov.voa.play.form.Condition

import java.text.DecimalFormat
import java.time.LocalDate
import scala.util.Try

private[forms] object LocalDateMapping {
  private[forms] val formKey = "value"
  private val startInfix     = "Start"
  private val endInfix       = "End"

  private val yearKey  = "year"
  private val monthKey = "month"
  private val dayKey   = "day"

  private val twoDigitFormatter  = new DecimalFormat("00")
  private val fourDigitFormatter = new DecimalFormat("0000")

  private val minValidDayOrMonth  = 1
  private val maxValidDay         = 31
  private val maxValidMonth       = 12
  private val minAllowedYearValue = 1000
  private val maxAllowedYearValue = 99999

  private[forms] def mapping(isStartDateForm: Boolean) = {
    val keyInfix = if (isStartDateForm) startInfix else endInfix

    Forms
      .tuple(dayKey -> dayMapping, monthKey -> monthMapping, yearKey -> yearMapping)
      // Fire error if all date fields are empty (isAnyFieldNotEmpty fails)
      // This will never fire if any individual field-level "missing" errors have fired (see dayMapping below)
      .verifying(
        s"authority${keyInfix}Date.error.required.all",
        date => isAnyFieldPopulated(Seq(date._1.trim, date._2.trim, date._3.trim))
      )
      // This error only fires if all fields have a value to check it's a valid date
      .verifying(
        "authorityDate.error.invalid",
        date => isValidDateOrAnyEmptyFields(date._1.trim, date._2.trim, date._3.trim)
      )
      .transform((formToLocalDate _).tupled, localDateToForm)
  }

  private def isValidDateOrAnyEmptyFields(day: String, month: String, year: String): Boolean =
    if (isAnyFieldEmpty(Seq(day, month, year))) true
    else Try(LocalDate.of(year.toInt, month.toInt, day.toInt)).isSuccess

  private def isAnyFieldPopulated(fields: Seq[String]): Boolean = fields.exists(_.nonEmpty)

  private def isAnyFieldEmpty(fields: Seq[String]): Boolean = fields.exists(_.isEmpty)

  private def isAnyFieldPopulatedCondition(fields: Seq[String]): Condition = mapping =>
    fields.exists(field => mapping.getOrElse(field, "").nonEmpty)

  private val dayMapping: Mapping[String] = AdditionalConstraintsMapping(
    text()
      .verifying("authorityDate.error.invalid.day", isEmptyOr(isInRange(minValidDayOrMonth, maxValidDay))),
    // Apply constraint if any field other than this one has a value (this stops field-level errors firing when all fields are empty)
    // Then fire error if this field is empty (nonEmpty constraint fails)
    // If all given fields are empty and constraint is not applied, this field either has a value
    // or will be caught downstream by "allEmpty" validation (see mapping above)
    Seq(
      ConditionalConstraint(
        isAnyFieldPopulatedCondition(Seq(s"$formKey.$monthKey", s"$formKey.$yearKey")),
        s"authorityDate.error.required.day",
        nonEmpty
      )
    )
  )

  private val monthMapping: Mapping[String] = AdditionalConstraintsMapping(
    text()
      .verifying("authorityDate.error.invalid.month", isEmptyOr(isInRange(minValidDayOrMonth, maxValidMonth))),
    Seq(
      ConditionalConstraint(
        isAnyFieldPopulatedCondition(Seq(s"$formKey.$dayKey", s"$formKey.$yearKey")),
        "authorityDate.error.required.month",
        nonEmpty
      )
    )
  )

  private val yearMapping: Mapping[String] = AdditionalConstraintsMapping(
    text()
      .verifying("authorityDate.error.invalid.year", isEmptyOr(isInRange(minAllowedYearValue, maxAllowedYearValue))),
    Seq(
      ConditionalConstraint(
        isAnyFieldPopulatedCondition(Seq(s"$formKey.$monthKey", s"$formKey.$dayKey")),
        "authorityDate.error.required.year",
        nonEmpty
      )
    )
  )

  private def formToLocalDate(day: String, month: String, year: String): LocalDate =
    LocalDate.of(year.trim.toInt, month.trim.toInt, day.trim.toInt)

  private def localDateToForm(date: LocalDate): (String, String, String) =
    (
      twoDigitFormatter.format(date.getDayOfMonth.toLong),
      twoDigitFormatter.format(date.getMonthValue.toLong),
      fourDigitFormatter.format(date.getYear.toLong)
    )
}
