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

import base.SpecBase
import models.domain._
import play.api.i18n.Messages

class CDSAccountSpec extends SpecBase {

  "CDSAccountSpec" must {
    "cashAccount can display correct message" in new Setup {
      val result = CDSAccount.formattedAccountType(cashAccount)
      result mustBe messages("remove.heading.caption.CdsCashAccount", cashAccount.number)
    }

    "dutyDeferment can display correct message" in new Setup {
      val result = CDSAccount.formattedAccountType(dutyDeferment)
      result mustBe messages("remove.heading.caption.CdsDutyDefermentAccount", dutyDeferment.number)
    }

    "generalGuarantee can display correct message" in new Setup {
      val result = CDSAccount.formattedAccountType(generalGuarantee)
      result mustBe messages("remove.heading.caption.CdsGeneralGuaranteeAccount", generalGuarantee.number)
    }
  }

  trait Setup {
    implicit val messages: Messages = messagesApi.preferred(fakeRequest())

    val cashAccount = CashAccount(
      "12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))

    val dutyDeferment = DutyDefermentAccount(
      "67890", "GB210987654321", AccountStatusOpen, DutyDefermentBalance(None, None, None, None))

    val generalGuarantee = GeneralGuaranteeAccount(
      "54321", "GB000000000000", AccountStatusOpen, Some(GeneralGuaranteeBalance(50.00, 50.00)))
  }
}