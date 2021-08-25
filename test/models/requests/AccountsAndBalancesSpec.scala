/*
 * Copyright 2021 HM Revenue & Customs
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

import models.domain
import models.domain.{AccountStatusClosed, CDSCashBalance, CashAccount, GeneralGuaranteeBalance}
import org.scalatest._

class AccountsAndBalancesSpec extends WordSpec with MustMatchers {

  val accountWithStatus: AccountWithStatus = AccountWithStatus("number", "type", "owner", AccountStatusClosed ,viewBalanceIsGranted = true)

  "GeneralGuaranteeAccount model" should {

    "correctly generate a domain model when a account, limit and balance are available" in {

      val expectedResult = domain.GeneralGuaranteeAccount("number", "owner", AccountStatusClosed, Some(GeneralGuaranteeBalance(BigDecimal(1), BigDecimal(2))))

      val generalGuaranteeAccount =
      GeneralGuaranteeAccount(account = accountWithStatus, guaranteeLimit = Some("1"), availableGuaranteeBalance = Some("2"))

      generalGuaranteeAccount.toDomain mustBe expectedResult
    }

    "correctly generate a domain model when limit and balance are not available" in {

      val expectedResult = domain.GeneralGuaranteeAccount("number", "owner", AccountStatusClosed, None)

      val generalGuaranteeAccount =
        GeneralGuaranteeAccount(account = accountWithStatus, guaranteeLimit = None, availableGuaranteeBalance = None)

      generalGuaranteeAccount.toDomain mustBe expectedResult
    }
  }

  "CdsCashAccount model" should {

    "correctly generate a domain model when account balance is available" in {

      val expectedResult = CashAccount("number", "owner", AccountStatusClosed, CDSCashBalance(Some(BigDecimal(1))))

      val cdsCashAccount = CdsCashAccount(accountWithStatus, Some("1"))

      cdsCashAccount.toDomain mustBe expectedResult
    }

    "correctly generate a domain model when account balance is unavailable" in {

      val expectedResult = CashAccount("number", "owner", AccountStatusClosed, CDSCashBalance(None))

      val cdsCashAccount = CdsCashAccount(accountWithStatus, None)

      cdsCashAccount.toDomain mustBe expectedResult
    }

  }
}
