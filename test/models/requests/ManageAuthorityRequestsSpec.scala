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

package models.requests

import base.SpecBase

class ManageAuthorityRequestsSpec extends SpecBase {

  "Accounts.hasOnlyDutyDefermentsAccount" should {
    "return true" when {
      "only dutyDeferments is present" in new Setup {
        ddAccountsOnly.hasOnlyDutyDefermentsAccount mustBe true
      }
    }

    "return false" when {
      "accounts other than dutyDeferments are present" in new Setup {
        accounts.hasOnlyDutyDefermentsAccount mustBe false
      }
    }
  }

  "Accounts.isDutyDefermentsAccountEmpty" should {
    "return true" when {
      "dutyDeferments account is not present" in new Setup {
        ddAccountsEmpty.isDutyDefermentsAccountEmpty mustBe true
      }
    }

    "return false" when {
      "dutyDeferments account is present" in new Setup {
        accounts.isDutyDefermentsAccountEmpty mustBe false
      }
    }
  }

  trait Setup {
    val ddAccountsOnly: Accounts  = Accounts(None, Seq("67890"), None)
    val ddAccountsEmpty: Accounts = Accounts(Some("12345"), Seq(), None)
    val accounts: Accounts        = Accounts(Some("12345"), Seq("67890"), Some("12345678"))
  }
}
