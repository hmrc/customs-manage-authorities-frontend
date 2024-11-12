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

import com.google.inject.Inject
import config.FrontendAppConfig
import utils.Constants._

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDate, LocalDateTime, LocalTime, OffsetDateTime, ZoneId, ZoneOffset}

class DateTimeService @Inject()(appConfig: FrontendAppConfig) {

  private val defaultZoneId = "Europe/London"

  def getTimeStamp: OffsetDateTime = OffsetDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)

  def systemTime(zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime = {
    if (appConfig.fixedDateTime) {
      LocalDateTime.of(
        LocalDate.of(
          FIXED_DATE_TIME_YEAR,
          FIXED_DATE_TIME_MONTH_OF_YEAR,
          FIXED_DATE_TIME_DAY_OF_MONTH),

        LocalTime.of(
          FIXED_DATE_TIME_HOUR_OF_DAY,
          FIXED_DATE_TIME_MIN_OF_HOUR)
      )
    } else {
      LocalDateTime.now(zoneId)
    }
  }

  def localTime(): LocalDateTime = {
    systemTime(ZoneId.of(defaultZoneId))
  }

  def localDate(): LocalDate = {
    systemTime(ZoneId.of(defaultZoneId)).toLocalDate
  }

  def isoLocalDateTime: String = DateTimeFormatter.ISO_INSTANT.format(Instant.now().truncatedTo(ChronoUnit.SECONDS))
}
