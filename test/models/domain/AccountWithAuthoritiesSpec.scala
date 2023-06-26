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

package models.domain

import base.SpecBase
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import java.time.LocalDate

class AccountWithAuthoritiesSpec extends SpecBase {
  "apply" must {
    "create the correct AccountWithAuthoritiesWithId object" in new SetUp {
      val accWithAuthWithIdOb: AccountWithAuthoritiesWithId = AccountWithAuthoritiesWithId(accWithAuthorities1)
      accWithAuthWithIdOb.accountType shouldBe CdsCashAccount
      accWithAuthWithIdOb.accountNumber shouldBe "123"
      accWithAuthWithIdOb.accountStatus shouldBe Option(AccountStatusOpen)
      accWithAuthWithIdOb.authorities.size should be > 0
    }
  }

  "AuthoritiesWithId.apply" must {
    "create the correct AuthoritiesWithId object" in new SetUp {
      val authsWithId: AuthoritiesWithId = AuthoritiesWithId(Seq(accWithAuthorities1, accWithAuthorities2))
      authsWithId.authorities.size should be > 0
      authsWithId.accounts.size should be > 0
      authsWithId.authorisedWithEori("GB123456789012").size should be > 0
    }
  }

  "AuthoritiesWithId.accounts" must {
    "return the correct result" in new SetUp {
      val authsWithId: AuthoritiesWithId = AuthoritiesWithId(Map(
        "key1" -> AccountWithAuthoritiesWithId(accWithAuthorities1),
        "key2" -> AccountWithAuthoritiesWithId(accWithAuthorities2)))

      authsWithId.accounts.size should be > 0
      authsWithIdWithEmptyMap.accounts.size shouldBe 0
    }
  }

  "AuthoritiesWithId.authorisedWithEori" must {
    "return the correct result" in new SetUp {
      val authsWithId: AuthoritiesWithId = AuthoritiesWithId(Map(
        "key1" -> AccountWithAuthoritiesWithId(accWithAuthorities1),
        "key2" -> AccountWithAuthoritiesWithId(accWithAuthorities2)))

      authsWithId.authorisedWithEori("GB123456789012").size should be > 0
      authsWithIdWithEmptyMap.authorisedWithEori("GB123456789012").size shouldBe 0
    }
  }

  trait SetUp {
    val emptyMap: Map[String, AccountWithAuthoritiesWithId] = Map()

    val authsWithIdWithEmptyMap: AuthoritiesWithId = AuthoritiesWithId(emptyMap)

    val standingAuthority1: StandingAuthority = StandingAuthority(
      "GB123456789012",
      LocalDate.now(),
      Option(LocalDate.now().plusDays(1)),
      viewBalance = true)

    val standingAuthority2: StandingAuthority = StandingAuthority(
      "GB123456789012",
      LocalDate.now(),
      Option(LocalDate.now().plusDays(1)),
      viewBalance = true)

    val accWithAuthorities1: AccountWithAuthorities = AccountWithAuthorities(CdsCashAccount,
      "123",
      Option(AccountStatusOpen),
      Seq(standingAuthority1, standingAuthority2))

    val accWithAuthorities2: AccountWithAuthorities = AccountWithAuthorities(CdsCashAccount,
      "123",
      Option(AccountStatusOpen),
      Seq(standingAuthority1, standingAuthority2))

    val authWithId = AuthoritiesWithId(Seq(accWithAuthorities1, accWithAuthorities2))
  }
}
