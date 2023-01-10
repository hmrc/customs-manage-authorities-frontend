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

package models

import models.domain.{AccountStatusOpen, GeneralGuaranteeAccount, GeneralGuaranteeBalance}
import org.scalatest._

class CustomsAccountsSpec extends WordSpec with MustMatchers {

  private val traderEori = "12345678"

  val guaranteeAccount = GeneralGuaranteeAccount("G123456", traderEori, AccountStatusOpen, Some(GeneralGuaranteeBalance(BigDecimal(1000000), BigDecimal(200000))))
  val guaranteeAccountZeroLimit = GeneralGuaranteeAccount("G123456", traderEori, AccountStatusOpen, Some(GeneralGuaranteeBalance(BigDecimal(0), BigDecimal(200001))))
  val guaranteeAccountZeroBalance = GeneralGuaranteeAccount("G123456", traderEori, AccountStatusOpen, Some(GeneralGuaranteeBalance(BigDecimal(200002), BigDecimal(0))))
  val guaranteeAccountZeroLimitZeroBalance = GeneralGuaranteeAccount("G123456", traderEori, AccountStatusOpen, Some(GeneralGuaranteeBalance(BigDecimal(0), BigDecimal(0))))


  "GeneralGuaranteeBalance" should {

    "return correct used funds value" in {
      val expectedUsedFunds = 800000
      guaranteeAccount.balances.get.usedFunds  must be (expectedUsedFunds)
    }

    "return zero used funds when the guarantee limit is zero" in {
      val expectedUsedFunds = -200001
      guaranteeAccountZeroLimit.balances.get.usedFunds  must be (expectedUsedFunds)
    }

    "return correct used percentage value" in {
      val expectedUsedPercentage = 80
      guaranteeAccount.balances.get.usedPercentage  must be (expectedUsedPercentage)
    }

    "return used funds of 100 percent when available balance is zero" in {
      val expectedUsedPercentage = 100
      guaranteeAccountZeroBalance.balances.get.usedPercentage  must be (expectedUsedPercentage)
    }

    "return zero used percentage and funds when available balance and limit are both zero" in {
      val expectedUsedPercentage = 0
      guaranteeAccountZeroLimitZeroBalance.balances.get.usedPercentage  must be (expectedUsedPercentage)
    }

  }

}
