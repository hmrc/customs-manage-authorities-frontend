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

import com.google.inject.Inject
import config.FrontendAppConfig

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDate, LocalDateTime, LocalTime, ZoneId}

class DateTimeService @Inject()(appConfig: FrontendAppConfig) {

  def systemTime(zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime = {
    if (appConfig.fixedDateTime) {
      LocalDateTime.of(LocalDate.of(2027, 12, 20), LocalTime.of(12,30)) // scalastyle:ignore
    }
    else {
      LocalDateTime.now(zoneId)
    }
  }

  def localTime(): LocalDateTime = {
    systemTime(ZoneId.of("Europe/London"))
  }

  def localDate(): LocalDate = {
    systemTime(ZoneId.of("Europe/London")).toLocalDate
  }

  def isoLocalDateTime = DateTimeFormatter.ISO_INSTANT.format(Instant.now().truncatedTo(ChronoUnit.SECONDS))
}
