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

package models.domain

import base.SpecBase
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import play.api.libs.json.{JsValue, Json}
import utils.TestData.FIVE

import java.time.LocalDate

class AccountWithAuthoritiesSpec extends SpecBase {
  "apply" must {
    "create the correct AccountWithAuthoritiesWithId object" in new SetUp {
      val accWithAuthWithIdOb: AccountWithAuthoritiesWithId = AccountWithAuthoritiesWithId(accWithAuthorities1)
      accWithAuthWithIdOb.accountType    shouldBe CdsCashAccount
      accWithAuthWithIdOb.accountNumber  shouldBe "123"
      accWithAuthWithIdOb.accountStatus  shouldBe Option(AccountStatusOpen)
      accWithAuthWithIdOb.authorities.size should be > 0
    }
  }

  "AuthoritiesWithId.apply" must {
    "create the correct AuthoritiesWithId object" in new SetUp {
      val authsWithId: AuthoritiesWithId = AuthoritiesWithId(Seq(accWithAuthorities1, accWithAuthorities2))
      authsWithId.authorities.size                          should be > 0
      authsWithId.accounts.size                             should be > 0
      authsWithId.authorisedWithEori("GB123456789012").size should be > 0
    }
  }

  "AuthoritiesWithId.accounts" must {
    "return the correct result" in new SetUp {
      val authsWithId: AuthoritiesWithId = AuthoritiesWithId(
        Map(
          "key1" -> AccountWithAuthoritiesWithId(accWithAuthorities1),
          "key2" -> AccountWithAuthoritiesWithId(accWithAuthorities2)
        )
      )

      authsWithId.accounts.size               should be > 0
      authsWithIdWithEmptyMap.accounts.size shouldBe 0
    }
  }

  "AuthoritiesWithId.authorisedWithEori" must {
    "return the correct result" in new SetUp {
      val authsWithId: AuthoritiesWithId = AuthoritiesWithId(
        Map(
          "key1" -> AccountWithAuthoritiesWithId(accWithAuthorities1),
          "key2" -> AccountWithAuthoritiesWithId(accWithAuthorities2)
        )
      )

      authsWithId.authorisedWithEori("GB123456789012").size               should be > 0
      authsWithIdWithEmptyMap.authorisedWithEori("GB123456789012").size shouldBe 0
    }
  }

  "AuthoritiesWithId.uniqueAuthorisedEORIs" must {
    "return the correct result" in new SetUp {
      val authsWithId: AuthoritiesWithId = AuthoritiesWithId(
        Map(
          "key1" -> AccountWithAuthoritiesWithId(accWithAuthorities1),
          "key2" -> AccountWithAuthoritiesWithId(accWithAuthorities2)
        )
      )

      authsWithId.accounts                          should have size 2
      authsWithId.uniqueAuthorisedEORIs             should have size 1
      authsWithIdWithEmptyMap.uniqueAuthorisedEORIs should have size 0
    }
  }

  "AccountWithAuthorities JSON Reads" must {
    "deserialize correctly from JSON" in {
      val jsonStr =
        s"""
           |{
           |  "accountType": "CDSCash",
           |  "accountNumber": "123",
           |  "accountStatus": "Open",
           |  "authorities": [
           |    {
           |      "authorisedEori": "GB123456789012",
           |      "authorisedFromDate": "${LocalDate.now()}",
           |      "authorisedToDate": "${LocalDate.now().plusDays(1)}",
           |      "viewBalance": true
           |    }
           |  ]
           |}
           |""".stripMargin

      val json   = Json.parse(jsonStr)
      val result = json.as[AccountWithAuthorities]

      result.accountType                     shouldBe CdsCashAccount
      result.accountNumber                   shouldBe "123"
      result.accountStatus                   shouldBe Some(AccountStatusOpen)
      result.authorities.head.authorisedEori shouldBe "GB123456789012"
    }
  }

  "AccountWithAuthoritiesWithId JSON Format" must {
    "serialize and deserialize correctly" in new SetUp {
      val accWithId: AccountWithAuthoritiesWithId = AccountWithAuthoritiesWithId(accWithAuthorities1)

      val json: JsValue                              = Json.toJson(accWithId)
      val deserialized: AccountWithAuthoritiesWithId = json.as[AccountWithAuthoritiesWithId]

      deserialized.accountType        shouldBe accWithId.accountType
      deserialized.accountNumber      shouldBe accWithId.accountNumber
      deserialized.accountStatus      shouldBe accWithId.accountStatus
      deserialized.authorities.keySet shouldBe accWithId.authorities.keySet
    }
  }

  "AuthoritiesWithId.uniqueAuthorisedEORIs" must {
    "return unique EORI values" in new SetUp {
      val differentEoriAuthority: StandingAuthority = StandingAuthority(
        "GB999999999999",
        LocalDate.now(),
        Some(LocalDate.now().plusDays(FIVE)),
        viewBalance = true
      )

      val accWithDifferentEori: AccountWithAuthorities = AccountWithAuthorities(
        CdsCashAccount,
        "456",
        Some(AccountStatusOpen),
        Seq(differentEoriAuthority)
      )

      val authsWithId: AuthoritiesWithId = AuthoritiesWithId(Seq(accWithAuthorities1, accWithDifferentEori))

      val result: Set[EORI] = authsWithId.uniqueAuthorisedEORIs
      result        should contain allOf ("GB123456789012", "GB999999999999")
      result.size shouldBe 2
    }
  }

  "AuthoritiesWithId JSON Format" must {
    "serialize and deserialize correctly" in new SetUp {
      val json: JsValue                   = Json.toJson(authWithId)
      val deserialized: AuthoritiesWithId = json.as[AuthoritiesWithId]

      deserialized.authorities.keySet shouldBe authWithId.authorities.keySet
      deserialized.accounts.size      shouldBe authWithId.accounts.size
    }
  }

  trait SetUp {
    val emptyMap: Map[String, AccountWithAuthoritiesWithId] = Map()

    val authsWithIdWithEmptyMap: AuthoritiesWithId = AuthoritiesWithId(emptyMap)

    val standingAuthority1: StandingAuthority =
      StandingAuthority("GB123456789012", LocalDate.now(), Option(LocalDate.now().plusDays(1)), viewBalance = true)

    val standingAuthority2: StandingAuthority =
      StandingAuthority("GB123456789012", LocalDate.now(), Option(LocalDate.now().plusDays(1)), viewBalance = true)

    val accWithAuthorities1: AccountWithAuthorities = AccountWithAuthorities(
      CdsCashAccount,
      "123",
      Option(AccountStatusOpen),
      Seq(standingAuthority1, standingAuthority2)
    )

    val accWithAuthorities2: AccountWithAuthorities = AccountWithAuthorities(
      CdsCashAccount,
      "123",
      Option(AccountStatusOpen),
      Seq(standingAuthority1, standingAuthority2)
    )

    val authWithId: AuthoritiesWithId = AuthoritiesWithId(Seq(accWithAuthorities1, accWithAuthorities2))
  }
}
