/*
 * Copyright 2022 HM Revenue & Customs
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

package utils

import play.api.i18n.Messages

import java.time.LocalDate

trait DateUtils {

  def latestOf(first: LocalDate, rest: LocalDate*): LocalDate =
    rest.fold(first)((a: LocalDate, b: LocalDate) => if (a.isAfter(b)) a else b)

  def earliestOf(first: LocalDate, rest: LocalDate*): LocalDate =
    rest.fold(first)((a: LocalDate, b: LocalDate) => if (a.isBefore(b)) a else b)

  def dateAsDayMonthAndYear(date: LocalDate)(implicit messages: Messages): String = s"${date.getDayOfMonth} ${dateAsMonth(date)} ${date.getYear}"

  def dateAsdMMMyyyy(date: LocalDate)(implicit messages: Messages): String = s"${date.getDayOfMonth} ${dateAsMonthAbbr(date)} ${date.getYear}"

  private def dateAsMonthAbbr(date: LocalDate)(implicit messages: Messages): String = messages(s"month.abbr.${date.getMonthValue}")
  private def dateAsMonth(date: LocalDate)(implicit messages: Messages): String = messages(s"month.${date.getMonthValue}")
}
