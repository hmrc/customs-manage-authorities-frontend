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

import models.domain.{CDSAccount, EORI}
import utils.Constants.{CASH_ACCOUNT_TYPE, GENERAL_GUARANTEE_ACCOUNT_TYPE}
import utils.StringUtils.gbEORIPrefix

case class AuthorisedAccounts(
  alreadyAuthorisedAccounts: Seq[CDSAccount],
  availableAccounts: Seq[CDSAccount],
  closedAccounts: Seq[CDSAccount],
  pendingAccounts: Seq[CDSAccount],
  enteredEori: EORI
)

object AuthorisedAccounts {
  def apply(
             alreadyAuthorisedAccounts: Seq[CDSAccount],
             availableAccounts: Seq[CDSAccount],
             closedAccounts: Seq[CDSAccount],
             pendingAccounts: Seq[CDSAccount],
             enteredEori: EORI
           ): AuthorisedAccounts = {

    def isNIOrCashOrGeneralGuaranteeAccountType(account: CDSAccount) =
      account.isNiAccount ||
        account.accountType.equals(CASH_ACCOUNT_TYPE) ||
        account.accountType.equals(GENERAL_GUARANTEE_ACCOUNT_TYPE)

    enteredEori match {
      case eori if eori.startsWith(gbEORIPrefix) =>
        new AuthorisedAccounts(
          alreadyAuthorisedAccounts,
          availableAccounts.filter(!_.isNiAccount),
          closedAccounts.filter(!_.isNiAccount),
          pendingAccounts.filter(!_.isNiAccount),
          enteredEori
        )
      case eori =>
        new AuthorisedAccounts(
          alreadyAuthorisedAccounts,
          availableAccounts.filter(isNIOrCashOrGeneralGuaranteeAccountType),
          closedAccounts.filter(isNIOrCashOrGeneralGuaranteeAccountType),
          pendingAccounts.filter(isNIOrCashOrGeneralGuaranteeAccountType),
          enteredEori
        )
    }
  }
}
