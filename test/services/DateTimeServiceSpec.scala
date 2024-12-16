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

import base.SpecBase
import config.FrontendAppConfig

import java.time.temporal.ChronoUnit
import java.time._
import java.time.format.DateTimeFormatter

class DateTimeServiceSpec() extends SpecBase {

  "DateTimeService" should {
    "return system date when fixed-systemdate-for-tests feature is disabled" in new Setup {
      val service               = new DateTimeService(appConfig)
      val result: LocalDateTime = service.systemTime()

      result.until(LocalDateTime.now(), ChronoUnit.SECONDS) mustBe <(1L)
    }

    "return iso local datetime" in new Setup {
      val service = new DateTimeService(appConfig)

      LocalDateTime.parse(
        service.isoLocalDateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")) mustBe a[LocalDateTime
      ]
    }

    "return local date when fixed-systemdate-for-tests feature is disabled" in new Setup {
      val service               = new DateTimeService(appConfig)
      val result: LocalDateTime = service.localTime()

      result.until(LocalDateTime.now(ZoneId.of("Europe/London")), ChronoUnit.SECONDS) mustBe <(1L)
    }

    "return date as 20-Dec-2027 when fixed-systemdate-for-tests feature is enabled" in {
      val year2027       = 2027
      val monthOfTheYear = 12
      val dayOfMonth     = 20
      val hourOfDay      = 12
      val minuteOfHour   = 30

      val app       = applicationBuilder()
        .configure("features.fixed-system-time" -> true)
        .build()
      val appConfig = app.injector.instanceOf[FrontendAppConfig]

      val service = new DateTimeService(appConfig)
      val result  = service.systemTime()

      result mustBe
        LocalDateTime.of(LocalDate.of(year2027, monthOfTheYear, dayOfMonth), LocalTime.of(hourOfDay, minuteOfHour))
    }
  }

  trait Setup {
    val app       = applicationBuilder().build()
    val appConfig = app.injector.instanceOf[FrontendAppConfig]
  }

}
