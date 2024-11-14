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

package pages.add

import org.scalacheck.Arbitrary
import pages.behaviours.PageBehaviours

import java.time.LocalDate

class AuthorityStartDatePageSpec extends PageBehaviours {

  "AuthorityStartDatePage" must {
    val year1900 = 1990
    val year2100 = 2100
    val firstMonthOfTheYear = 1
    val firstDayOfMonth = 1

    implicit lazy val arbitraryLocalDate: Arbitrary[LocalDate] = Arbitrary {
      datesBetween(
        LocalDate.of(year1900, firstMonthOfTheYear, firstDayOfMonth),
        LocalDate.of(year2100, firstMonthOfTheYear, firstMonthOfTheYear)
      )
    }

    beRetrievable[LocalDate](AuthorityStartDatePage)

    beSettable[LocalDate](AuthorityStartDatePage)

    beRemovable[LocalDate](AuthorityStartDatePage)
  }
}
