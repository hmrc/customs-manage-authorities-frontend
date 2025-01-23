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

package models

import base.SpecBase
import models.domain.{
  AccountStatusClosed, AccountStatusOpen, AccountStatusPending, AccountStatusSuspended, CDSAccounts, CDSCashBalance,
  CashAccount, DutyDefermentAccount, DutyDefermentBalance, GeneralGuaranteeAccount, GeneralGuaranteeBalance
}
import play.api.libs.json._

class CustomsAccountsSpec extends SpecBase {

  "GeneralGuaranteeBalance" should {

    "return correct used funds value" in new Setup {
      val expectedUsedFunds = 800000
      guaranteeAccount.balances.get.usedFunds must be(expectedUsedFunds)
    }

    "return zero used funds when the guarantee limit is zero" in new Setup {
      val expectedUsedFunds: Int = -200001
      guaranteeAccountZeroLimit.balances.get.usedFunds must be(expectedUsedFunds)
    }

    "return correct used percentage value" in new Setup {
      val expectedUsedPercentage = 80
      guaranteeAccount.balances.get.usedPercentage must be(expectedUsedPercentage)
    }

    "return used funds of 100 percent when available balance is zero" in new Setup {
      val expectedUsedPercentage = 100
      guaranteeAccountZeroBalance.balances.get.usedPercentage must be(expectedUsedPercentage)
    }

    "return zero used percentage and funds when available balance and limit are both zero" in new Setup {
      val expectedUsedPercentage = 0
      guaranteeAccountZeroLimitZeroBalance.balances.get.usedPercentage must be(expectedUsedPercentage)
    }
  }

  "DutyDefermentAccount" should {
    "return correct order after compare while sorting" in {
      val ddAccn1 = DutyDefermentAccount(
        "123",
        "XI9876543210000",
        AccountStatusOpen,
        DutyDefermentBalance(Some(100.00), Some(100.00), Some(100.00), Some(100.00)),
        isNiAccount = true
      )

      val ddAccn2 = DutyDefermentAccount(
        "124",
        "XI9876543210000",
        AccountStatusOpen,
        DutyDefermentBalance(Some(100.00), Some(100.00), Some(100.00), Some(100.00)),
        isNiAccount = true
      )

      ddAccn2 > ddAccn1 mustBe true
      ddAccn1.accountType mustBe "dutyDeferment"
    }
  }

  "CashAccount" should {
    "return correct accountType value" in {
      val openCashAccount: CashAccount =
        CashAccount("23456", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(100.00)))

      openCashAccount.accountType mustBe "cash"
    }
  }

  "CDSAccountStatusReads and CDSAccountStatusWrites" should {
    "return the correct output for CDSAccountStatusReads" in {
      import models.domain.CDSAccountStatus.CDSAccountStatusReads

      Json.fromJson(Json.parse("\"Open\"")) mustBe JsSuccess(AccountStatusOpen)
      Json.fromJson(Json.parse("\"Suspended\"")) mustBe JsSuccess(AccountStatusSuspended)
      Json.fromJson(Json.parse("\"Closed\"")) mustBe JsSuccess(AccountStatusClosed)
      Json.fromJson(Json.parse("\"Pending\"")) mustBe JsSuccess(AccountStatusPending)
      Json.fromJson(Json.parse("\"Invalid\"")) mustBe JsSuccess(AccountStatusOpen)
    }

    "return the correct output for CDSAccountStatusWrites" in {
      import models.domain.CDSAccountStatus.CDSAccountStatusWrites

      CDSAccountStatusWrites.writes(AccountStatusOpen) mustBe Json.parse("\"Open\"")
      CDSAccountStatusWrites.writes(AccountStatusClosed) mustBe Json.parse("\"Closed\"")
      CDSAccountStatusWrites.writes(AccountStatusSuspended) mustBe Json.parse("\"Suspended\"")
      CDSAccountStatusWrites.writes(AccountStatusPending) mustBe Json.parse("\"Pending\"")
    }
  }

  "CDSAccounts.alreadyAuthorised" should {
    "return the correct output" in {

      val openCashAccount: CashAccount   =
        CashAccount("23456", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(100.00)))
      val closedCashAccount: CashAccount =
        CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))

      val cdsAccounts: CDSAccounts = CDSAccounts("GB123456789012", List(openCashAccount, closedCashAccount))

      cdsAccounts.alreadyAuthorised(Seq("12345")) mustBe Seq(closedCashAccount)
    }
  }

  "CDSAccounts.canAuthoriseAccounts" should {
    "return the correct output" in {

      val openCashAccount: CashAccount   =
        CashAccount("23456", "GB123456789012", AccountStatusClosed, CDSCashBalance(Some(100.00)))
      val closedCashAccount: CashAccount =
        CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))

      val cdsAccounts: CDSAccounts = CDSAccounts("GB123456789012", List(openCashAccount, closedCashAccount))

      cdsAccounts.canAuthoriseAccounts(Seq("12345")) mustBe Seq()
    }
  }

  trait Setup {
    private val traderEori = "12345678"

    val zeroAmount                     = 0
    val guaranteeAmtLimit              = 1000000
    val guaranteeAmtAvlBal             = 200000
    val guaranteeAmtAvlBalForZeroLimit = 200001
    val guaranteeAmtAvlBalForZeroBal   = 200002

    val guaranteeAccount: GeneralGuaranteeAccount =
      GeneralGuaranteeAccount(
        "G123456",
        traderEori,
        AccountStatusOpen,
        Some(GeneralGuaranteeBalance(BigDecimal(guaranteeAmtLimit), BigDecimal(guaranteeAmtAvlBal)))
      )

    val guaranteeAccountZeroLimit: GeneralGuaranteeAccount =
      GeneralGuaranteeAccount(
        "G123456",
        traderEori,
        AccountStatusOpen,
        Some(GeneralGuaranteeBalance(BigDecimal(zeroAmount), BigDecimal(guaranteeAmtAvlBalForZeroLimit)))
      )

    val guaranteeAccountZeroBalance: GeneralGuaranteeAccount =
      GeneralGuaranteeAccount(
        "G123456",
        traderEori,
        AccountStatusOpen,
        Some(GeneralGuaranteeBalance(BigDecimal(guaranteeAmtAvlBalForZeroBal), BigDecimal(zeroAmount)))
      )

    val guaranteeAccountZeroLimitZeroBalance: GeneralGuaranteeAccount =
      GeneralGuaranteeAccount(
        "G123456",
        traderEori,
        AccountStatusOpen,
        Some(GeneralGuaranteeBalance(BigDecimal(zeroAmount), BigDecimal(zeroAmount)))
      )
  }
}
