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

package utils

import base.SpecBase

class StringUtilsSpec extends SpecBase {

  val stringWithLeadingSpace = "  LeadingSpaceStr"
  val stringWithTrailingSpace = "TrailingSpaceStr   "
  val stringWithLeadingAndTrailingSpace = "  TrailingAndLeading1234Str   "
  val stringWithSpacesIncludingLeadingAndTrailing = "  Spaces inc leading and trailing spaces  "
  val stringWithNoSpace = "No_Spaces_Str_123"

  "removeSpacesFromString" should {
    "remove the leading spaces from the input string" in {
      StringUtils.removeSpacesFromString(stringWithLeadingSpace) mustBe "LeadingSpaceStr"
    }

    "remove the trailing spaces from the input string" in {
      StringUtils.removeSpacesFromString(stringWithTrailingSpace) mustBe "TrailingSpaceStr"
    }

    "remove all the spaces including leading and trailing from the input string" in {
      StringUtils.removeSpacesFromString(stringWithLeadingAndTrailingSpace) mustBe "TrailingAndLeading1234Str"
    }

    "remove all the spaces from the input string" in {
      StringUtils.removeSpacesFromString(stringWithSpacesIncludingLeadingAndTrailing) mustBe
        "Spacesincleadingandtrailingspaces"
    }

    "return the unchanged string when there is no space in the string" in {
      StringUtils.removeSpacesFromString(stringWithNoSpace) mustBe stringWithNoSpace
    }
  }
}
