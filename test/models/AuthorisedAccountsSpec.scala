/*
 * Copyright 2025 HM Revenue & Customs
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
  AccountStatusOpen, CDSCashBalance, CashAccount, DutyDefermentAccount, DutyDefermentBalance, GeneralGuaranteeAccount
}

class AuthorisedAccountsSpec extends SpecBase {

  "AuthorisedAccounts.apply" when {

    "the EORI starts with GB" should {
      "exclude NI accounts from available, closed, and pending" in new Setup {
        val result = AuthorisedAccounts(input, input, input, input, "GB123456789012")

        result.alreadyAuthorisedAccounts mustBe input
        result.availableAccounts must contain theSameElementsAs Seq(ddGb, cash, guar)
        result.closedAccounts    must contain theSameElementsAs Seq(ddGb, cash, guar)
        result.pendingAccounts   must contain theSameElementsAs Seq(ddGb, cash, guar)
      }
    }

    "the EORI does not start with GB" should {
      "keep only NI, cash, or general guarantee accounts" in new Setup {
        val result = AuthorisedAccounts(input, input, input, input, "XI123456789012")

        result.alreadyAuthorisedAccounts mustBe input
        result.availableAccounts must contain theSameElementsAs Seq(ddNi, cash, guar)
        result.closedAccounts    must contain theSameElementsAs Seq(ddNi, cash, guar)
        result.pendingAccounts   must contain theSameElementsAs Seq(ddNi, cash, guar)
      }
    }
  }

  trait Setup {
    val ddNi  =
      DutyDefermentAccount("DD", "owner", AccountStatusOpen, DutyDefermentBalance(None, None, None, None), true)
    val ddGb  = DutyDefermentAccount("DD", "owner", AccountStatusOpen, DutyDefermentBalance(None, None, None, None))
    val cash  = CashAccount("CASH", "owner", AccountStatusOpen, CDSCashBalance(None))
    val guar  = GeneralGuaranteeAccount("GUA", "owner", AccountStatusOpen, None)
    val input = Seq(ddNi, ddGb, cash, guar)
  }
}
