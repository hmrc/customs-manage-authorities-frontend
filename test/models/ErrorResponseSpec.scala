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

package models

import base.SpecBase

class ErrorResponseSpec extends SpecBase {

  "ErrorResponse" must {
    "show correct error message for EORIValidationError" in {
      val result = EORIValidationError
      result.msg mustBe "Failed to get validation for EORI"
    }

    "show correct error message for MissingAuthorityError" in {
      val result = MissingAuthorityError
      result.msg mustBe "Missing authority"
    }

    "show correct error message for MissingAuthorisedUser" in {
      val result = MissingAuthorisedUser
      result.msg mustBe "Missing authorised user on revoke"
    }

    "show correct error message for MissingAccountError" in {
      val result = MissingAccountError
      result.msg mustBe "Missing account"
    }

    "show correct error message for EmptyAccountsError" in {
      val result = EmptyAccountsError
      result.msg mustBe "Empty accounts list of ShowBalance page load"
    }

    "show correct error message for NoStartDateError" in {
      val result = NoStartDateError
      result.msg mustBe "No data present for the user's start date"
    }

    "show correct error message for UnknownAccountType" in {
      val result = UnknownAccountType
      result.msg mustBe "Unknown account type"
    }

    "show correct error message for ShowBalanceError" in {
      val result = ShowBalanceError
      result.msg mustBe "No show balance data found"
    }

    "show correct error message for SubmissionError" in {
      val result = SubmissionError
      result.msg mustBe "Revoke authority request submission to backend failed"
    }

    "show correct error message for StartDateError" in {
      val result = StartDateError
      result.msg mustBe "No data present for the user's start date"
    }
  }
}