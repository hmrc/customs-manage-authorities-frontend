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

package controllers

import base.SpecBase
import models.domain.{AuthorisedUser, StandingAuthority}
import models.requests.{Accounts, AddAuthorityRequest}

import java.time.LocalDate

class PackageSpec extends SpecBase {
  "addAuthRequestList" should {
    "return correct output" in new Setup {
      addAuthRequestList(authReq) mustBe List(authReqForDDAccount, authReqForCashAndGuaranteeAccount)
    }

    "request list with only one request when accounts has only duty deferments account" in new Setup {
      addAuthRequestList(authReqWithDDAccountsOnly) mustBe List(authReqWithDDAccountsOnly)
    }

    "request list with only one request when accounts has no duty deferments account" in new Setup {
      addAuthRequestList(authReqWithDDAccountsOnly) mustBe List(authReqWithDDAccountsOnly)
    }
  }

  trait Setup {
    val localDate: LocalDate = LocalDate.now()
    val toLocalDate: LocalDate = LocalDate.now().plusDays(1)

    val authReq: AddAuthorityRequest = AddAuthorityRequest(
      Accounts(Some("12345"), Seq("67890"), Some("12345678")),
      StandingAuthority("GB123456789012", localDate, Option(toLocalDate), viewBalance = true),
      AuthorisedUser("name", "job")
    )

    val authReqWithDDAccountsOnly: AddAuthorityRequest = AddAuthorityRequest(
      Accounts(None, Seq("67890"), None),
      StandingAuthority("GB123456789012", localDate, Option(toLocalDate), viewBalance = true),
      AuthorisedUser("name", "job")
    )

    val authReqForCashAndGuaranteeAccount: AddAuthorityRequest = AddAuthorityRequest(
      Accounts(Some("12345"), Seq(), Some("12345678")),
      StandingAuthority("GB123456789012", localDate, Option(toLocalDate), viewBalance = true),
      AuthorisedUser("name", "job")
    )

    val authReqForDDAccount: AddAuthorityRequest = AddAuthorityRequest(
      Accounts(None, Seq("67890"), None),
      StandingAuthority("GB123456789012", localDate, Option(toLocalDate), viewBalance = true),
      AuthorisedUser("name", "job")
    )
  }
}
