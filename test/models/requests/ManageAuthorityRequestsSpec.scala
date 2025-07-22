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
import models.domain.{AuthorisedUser, CdsCashAccount, StandingAuthority}
import play.api.libs.json.Json

import java.time.LocalDate

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

  "Accounts JSON format" should {
    "serialize and deserialize correctly" in new Setup {
      Json.toJson(accounts).as[Accounts] mustBe accounts
    }
  }

  "AddAuthorityRequest JSON format" should {
    "serialize and deserialize correctly" in new Setup {
      Json.toJson(addAuthorityRequest).as[AddAuthorityRequest] mustBe addAuthorityRequest
    }
  }

  "RevokeAuthorityRequest" should {
    "serialize and deserialize correctly" in new Setup {
      Json.toJson(revokeAuthorityRequest).as[RevokeAuthorityRequest] mustBe revokeAuthorityRequest
    }
  }

  trait Setup {
    val ddAccountsOnly: Accounts  = Accounts(None, Seq("67890"), None)
    val ddAccountsEmpty: Accounts = Accounts(Some("12345"), Seq(), None)

    val accounts: Accounts = Accounts(Some("12345"), Seq("67890"), Some("12345678"))

    val standingAuthority: StandingAuthority =
      StandingAuthority("GB123456789000", LocalDate.now, None, viewBalance = true)
    val authorisedUser: AuthorisedUser       = AuthorisedUser("test_name", "test_role")

    val addAuthorityRequest: AddAuthorityRequest =
      AddAuthorityRequest(accounts, standingAuthority, authorisedUser, true, "GB12345")

    val revokeAuthorityRequest: RevokeAuthorityRequest = RevokeAuthorityRequest(
      "ACC123",
      CdsCashAccount,
      "GB54321",
      authorisedUser,
      "GB12345"
    )
  }
}
