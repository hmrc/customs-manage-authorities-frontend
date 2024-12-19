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

package utils

import base.SpecBase
import play.api.i18n.Messages
import play.api.test.Helpers
import utils.StringUtils.singleSpace

import java.time.LocalDate

class DateUtilsSpec extends SpecBase {
  "latestOf" should {
    "return the correct latest date" in new Setup {
      new DateUtils {}.latestOf(olderDate, newerDate) mustBe newerDate
      new DateUtils {}.latestOf(olderDate, newerDate, newestDate) mustBe newestDate
      new DateUtils {}.latestOf(olderDate, newestDate, newerDate) mustBe newestDate
    }

    "return the input date as latest if only one date is passed" in new Setup {
      new DateUtils {}.latestOf(date) mustBe date
    }
  }

  "earliestOf" should {
    "return the correct earliest date" in new Setup {
      new DateUtils {}.earliestOf(olderDate, newerDate) mustBe olderDate
      new DateUtils {}.earliestOf(olderDate, newerDate, newestDate) mustBe olderDate
      new DateUtils {}.earliestOf(newerDate, olderDate, newestDate) mustBe olderDate
    }

    "return the input date as earliest if only one date is passed" in new Setup {
      new DateUtils {}.earliestOf(date) mustBe date
    }
  }

  "dateAsDayMonthAndYear" should {
    "return the correct string" in new Setup {
      new DateUtils {}.dateAsDayMonthAndYear(date) mustBe s"15${singleSpace}month.5${singleSpace}2023"
    }
  }

  "dateAsdMMMyyyy" should {
    "return the correct string" in new Setup {
      new DateUtils {}.dateAsdMMMyyyy(date) mustBe s"15${singleSpace}month.abbr.5${singleSpace}2023"
    }
  }
}

trait Setup {
  val year2023               = 2023
  val fifthMonthOfTheYear    = 5
  val fifteenthDayOfMonth    = 15
  val twentiethDayOfMonth    = 20
  val twentySecondDayOfMonth = 22

  val olderDate: LocalDate  = LocalDate.of(year2023, fifthMonthOfTheYear, fifteenthDayOfMonth)
  val newerDate: LocalDate  = LocalDate.of(year2023, fifthMonthOfTheYear, twentiethDayOfMonth)
  val newestDate: LocalDate = LocalDate.of(year2023, fifthMonthOfTheYear, twentySecondDayOfMonth)
  val date: LocalDate       = LocalDate.of(year2023, fifthMonthOfTheYear, fifteenthDayOfMonth)

  implicit val messages: Messages = Helpers.stubMessages()
}
