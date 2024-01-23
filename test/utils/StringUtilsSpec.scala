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
import utils.StringUtils._

class StringUtilsSpec extends SpecBase {

  val stringWithLeadingSpace = "  LeadingSpaceStr"
  val stringWithTrailingSpace = "TrailingSpaceStr   "
  val stringWithLeadingAndTrailingSpace = "  TrailingAndLeading1234Str   "
  val stringWithSpacesIncludingLeadingAndTrailing = "  Spaces inc leading and trailing spaces  "
  val stringWithNoSpace = "No_Spaces_Str_123"

  "emptyString" should {
    "return empty string with no space" in {
      emptyString mustBe "".trim
    }
  }

  "singleSpace" should {
    "return string with single space" in {
      singleSpace mustBe " "
    }
  }

  "nIEORIPrefix" should {
    "return the correct Prefix for NI EORI" in {
      nIEORIPrefix mustBe "XI"
    }
  }

  "htmlSingleLineBreak" should {
    "return the correct html line break string" in {
      htmlSingleLineBreak mustBe "<br>"
    }
  }

  "removeSpacesFromString" should {
    "remove the leading spaces from the input string" in {
      removeSpacesFromString(stringWithLeadingSpace) mustBe "LeadingSpaceStr"
    }

    "remove the trailing spaces from the input string" in {
      removeSpacesFromString(stringWithTrailingSpace) mustBe "TrailingSpaceStr"
    }

    "remove all the spaces including leading and trailing from the input string" in {
      removeSpacesFromString(stringWithLeadingAndTrailingSpace) mustBe "TrailingAndLeading1234Str"
    }

    "remove all the spaces from the input string" in {
      removeSpacesFromString(stringWithSpacesIncludingLeadingAndTrailing) mustBe
        "Spacesincleadingandtrailingspaces"
    }

    "return the unchanged string when there is no space in the string" in {
      removeSpacesFromString(stringWithNoSpace) mustBe stringWithNoSpace
    }
  }

  "removeSpaceAndConvertToUpperCase" should {
    "return correct output" in {
      removeSpaceAndConvertToUpperCase("gb12 345678") mustBe "GB12345678"
      removeSpaceAndConvertToUpperCase("gb12 3 45678") mustBe "GB12345678"
      removeSpaceAndConvertToUpperCase("xi12 3 45678") mustBe "XI12345678"
      removeSpaceAndConvertToUpperCase("XI12 3 45678") mustBe "XI12345678"
      removeSpaceAndConvertToUpperCase(" XI12 3 45678 ") mustBe "XI12345678"
      removeSpaceAndConvertToUpperCase(" xI12 3 45 678 ") mustBe "XI12345678"
      removeSpaceAndConvertToUpperCase("GB12 3 45678") mustBe "GB12345678"
      removeSpaceAndConvertToUpperCase("gB 12 3 4567 89012") mustBe "GB123456789012"
      removeSpaceAndConvertToUpperCase("xI 12 3 4567 89012") mustBe "XI123456789012"
      removeSpaceAndConvertToUpperCase("12 3 4567 89012") mustBe "123456789012"
    }
  }

  "isXIEori" should {
    "return true when input strung starts with XI" in {
      isXIEori("XI123456789012") mustBe true
      isXIEori("XI12345678") mustBe true
      isXIEori("XI") mustBe true
    }

    "return false when input string does not starts with XI" in {
      isXIEori("GB123456789012") mustBe false
      isXIEori("xi12345678") mustBe false
      isXIEori("xi") mustBe false
    }
  }

  "gbEORIPrefix" should {
    "return correct value" in {
      gbEORIPrefix mustBe "GB"
    }
  }

  "comma" should {
    "return correct value" in {
      comma mustBe ","
    }
  }

  "hyphenWithSpaces" should {
    "return correct value" in {
      hyphenWithSpaces mustBe " - "
    }
  }

  "htmlSingleLineBreak" should {
    "return correct value" in {
      htmlSingleLineBreak mustBe "<br>"
    }
  }
}
