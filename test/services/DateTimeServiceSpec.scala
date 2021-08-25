/*
 * Copyright 2021 HM Revenue & Customs
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

class DateTimeServiceSpec() extends SpecBase {

  "DateTimeService" should {
    "return system date when fixed-systemdate-for-tests feature is disabled" in new Setup {
      val service = new DateTimeService(appConfig)
      val result = service.systemTime()
      result.until(LocalDateTime.now(), ChronoUnit.SECONDS) mustBe <(1L)
    }

    "return local date when fixed-systemdate-for-tests feature is disabled" in  new Setup {
      val service = new DateTimeService(appConfig)
      val result = service.localTime()
      result.until(LocalDateTime.now(ZoneId.of("Europe/London")), ChronoUnit.SECONDS) mustBe <(1L)
    }

    "return date as 20-Dec-2027 when fixed-systemdate-for-tests feature is enabled" in {
      val app = applicationBuilder()
        .configure("features.fixed-system-time" -> true)
        .build()
      val appConfig = app.injector.instanceOf[FrontendAppConfig]

      val service = new DateTimeService(appConfig)
      val result = service.systemTime()
      result mustBe LocalDateTime.of(LocalDate.of(2027,12,20), LocalTime.of(12,30))
    }
  }

  trait Setup {
    val app = applicationBuilder().build()
    val appConfig = app.injector.instanceOf[FrontendAppConfig]
  }

}
