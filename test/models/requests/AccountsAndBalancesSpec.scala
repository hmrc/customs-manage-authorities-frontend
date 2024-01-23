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

package models.requests

import base.SpecBase
import models.domain
import models.domain.{AccountStatusClosed, CDSCashBalance, CashAccount, DutyDefermentBalance, GeneralGuaranteeBalance}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{EitherValues, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import utils.WireMockHelper

class AccountsAndBalancesSpec extends SpecBase
  with WireMockHelper
  with ScalaFutures
  with IntegrationPatience
  with EitherValues
  with OptionValues
  with MockitoSugar {

  val accountWithStatus: AccountWithStatus =
    AccountWithStatus("number", "type", "owner", AccountStatusClosed, viewBalanceIsGranted = true)

  "GeneralGuaranteeAccount model" should {

    "correctly generate a domain model when a account, limit and balance are available" in {

      val expectedResult =
        domain.GeneralGuaranteeAccount(
          "number", "owner", AccountStatusClosed, Some(GeneralGuaranteeBalance(BigDecimal(1), BigDecimal(2))))

      val generalGuaranteeAccount =
        GeneralGuaranteeAccount(
          account = accountWithStatus,
          guaranteeLimit = Some("1"),
          availableGuaranteeBalance = Some("2"))

      generalGuaranteeAccount.toDomain mustBe expectedResult
    }

    "correctly generate a domain model when limit and balance are not available" in {

      val expectedResult = domain.GeneralGuaranteeAccount("number", "owner", AccountStatusClosed, None)

      val generalGuaranteeAccount =
        GeneralGuaranteeAccount(
          account = accountWithStatus,
          guaranteeLimit = None,
          availableGuaranteeBalance = None)

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

  "DutyDefermentAccount model" should {

    "correctly generate a domain model when a account, limit and balance are available" in {
      val amount = 4

      val guaranteeLimit = BigDecimal(1)
      val accountLimit = BigDecimal(2)
      val availableGuaranteeBalance = BigDecimal(3)
      val availableAccountBalance = BigDecimal(amount)

      val expectedResult =
        domain.DutyDefermentAccount(
          "number",
          "owner",
          AccountStatusClosed,
          DutyDefermentBalance(
            Some(guaranteeLimit),
            Some(accountLimit),
            Some(availableGuaranteeBalance),
            Some(availableAccountBalance)),
          isNiAccount = true)

      val dutyDefermentAccount =
        DutyDefermentAccount(
          account = accountWithStatus,
          isNiAccount = Some(true),
          isIomAccount = Some(false),
          limits = Some(Limits("1", "2")),
          balances = Some(DefermentBalances("3", "4"))
        )

      dutyDefermentAccount.toDomain() mustBe expectedResult
    }

    "correctly generate a domain model when limit and balance are not available" in {

      val expectedResult =
        domain.DutyDefermentAccount(
          "number",
          "owner",
          AccountStatusClosed,
          DutyDefermentBalance(None, None, None, None)
        )

      val dutyDefermentAccount =
        DutyDefermentAccount(
          account = accountWithStatus,
          isNiAccount = Some(false),
          isIomAccount = Some(false),
          limits = None,
          balances = None)

      dutyDefermentAccount.toDomain() mustBe expectedResult
    }
  }
}
