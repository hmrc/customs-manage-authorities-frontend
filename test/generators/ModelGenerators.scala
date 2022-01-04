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

package generators

import models._
import models.domain.{AccountStatusOpen, AuthorisedUser, CDSAccount, CDSCashBalance, CashAccount}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {
  self: Generators =>

  implicit lazy val arbitraryShowBalance: Arbitrary[ShowBalance] =
    Arbitrary {
      Gen.oneOf(ShowBalance.values.toSeq)
    }
  
  implicit lazy val arbitraryAuthorityStart: Arbitrary[AuthorityStart] =
    Arbitrary {
      Gen.oneOf(AuthorityStart.values.toSeq)
    }

  implicit lazy val arbitraryAuthorisedUser: Arbitrary[AuthorisedUser] =
    Arbitrary {
      for {
        name <- stringsWithMaxLength(50)
        role <- stringsWithMaxLength(50)
      } yield AuthorisedUser(name, role)
    }

  implicit lazy val arbitraryEoriNumber: Arbitrary[CompanyDetails] =
    Arbitrary {
      for {
        name <- stringsWithMaxLength(50)
      } yield CompanyDetails(name, None)
    }

  val genCashAccount: Gen[CashAccount] = for {
    number <- Gen.choose(1, 10000)
    owner <- Gen.choose(100000000000L, 999999999999L)
    balance <- arbitrary[BigDecimal]
  } yield CashAccount(number.toString, s"GB$owner", AccountStatusOpen, CDSCashBalance(Some(balance)))

  implicit lazy val arbitraryCashAccount: Arbitrary[CashAccount] =
    Arbitrary {
      genCashAccount
    }

  implicit lazy val arbitraryCdsAccount: Arbitrary[CDSAccount] =
    Arbitrary {
      genCashAccount.map(_.asInstanceOf[CDSAccount])
    }

}
