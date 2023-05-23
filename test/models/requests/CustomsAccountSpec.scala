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
import models.domain.{DutyDefermentBalance, GeneralGuaranteeBalance}

import scala.math.Numeric.BigDecimalIsFractional.zero

class CustomsAccountSpec extends SpecBase {

  "GeneralGuaranteeBalance" should {
    "used funds must be correctly calculated" in new Setup {
      val res = GeneralGuaranteeBalance(guaranteeLimit, availableGuaranteeBalance)
      res.usedFunds mustBe usedFunds
    }

    "used funds must not be equal when calculated with incorrect data" in new Setup {
      val res = GeneralGuaranteeBalance(guaranteeLimit + 1.00, availableGuaranteeBalance)
      res.usedFunds must not be usedFunds
    }

    "used percentages must be correctly calculated" in new Setup {
      val res = GeneralGuaranteeBalance(guaranteeLimit, availableGuaranteeBalance)
      res.usedPercentage mustBe 50
    }

    "used percentages must be zero when calculated with incorrect data" in new Setup {
      val res = GeneralGuaranteeBalance(guaranteeLimit, availableGuaranteeBalance + 1.00)
      res.usedPercentage mustBe 0
    }

    "used percentages must hanlde a divide by zero scenario" in new Setup {
      val res = GeneralGuaranteeBalance(0, availableGuaranteeBalance)
      res.usedPercentage mustBe 0
    }
  }

  "DutyDefermentBalance" should {
    "Populate duty deferement balance correctly" in new Setup {
      val res = DutyDefermentBalance(Some(1), Some(1), Some(1), Some(1))
      res mustBe dutyDefermentBalance
    }

    "A incorrectly populated duty deferement balance must not match" in new Setup {
      val res = DutyDefermentBalance(Some(2), Some(1), Some(1), Some(1))
      res must not be dutyDefermentBalance
    }
  }

  trait Setup {
    val guaranteeLimit: BigDecimal = 2.00
    val availableGuaranteeBalance: BigDecimal = 1.00
    val usedFunds: BigDecimal = guaranteeLimit - availableGuaranteeBalance
    val usedPercentage: BigDecimal = if (guaranteeLimit.compare(zero) == 0) {
      zero } else { usedFunds / guaranteeLimit * 100 }

    val periodGuaranteeLimit: BigDecimal = 1.00
    val periodAccountLimit: BigDecimal = 1.00
    val periodAvailableGuaranteeBalance: BigDecimal = 1.00
    val periodAvailableAccountBalance: BigDecimal = 1.00

    val dutyDefermentBalance: DutyDefermentBalance = DutyDefermentBalance(Some(periodGuaranteeLimit),
      Some(periodAccountLimit), Some(periodAvailableGuaranteeBalance), Some(periodAvailableAccountBalance))
  }
}
