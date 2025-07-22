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
import models.domain.*
import play.api.i18n.Messages
import play.api.libs.json.{JsString, JsSuccess, JsValue, Json}

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

    "deserialize 'Pending' CDSAccountStatus" in {
      val json = JsString("Pending")
      json.validate[CDSAccountStatus] mustBe JsSuccess(AccountStatusPending)
    }

    "serialize and deserialize DutyDefermentBalance" in new Setup {
      val dutyDefermentBalance: DutyDefermentBalance = DutyDefermentBalance(Some(ten), Some(ten), Some(ten), Some(ten))
      val json: JsValue                              = Json.toJson(dutyDefermentBalance)
      json.validate[DutyDefermentBalance].get mustBe dutyDefermentBalance
    }

    "serialize and deserialize GeneralGuaranteeBalance" in new Setup {
      val generalGuaranteeBalance: GeneralGuaranteeBalance = GeneralGuaranteeBalance(ten, ten)
      val json: JsValue                                    = Json.toJson(generalGuaranteeBalance)
      json.validate[GeneralGuaranteeBalance].get mustBe generalGuaranteeBalance
    }

    "serialize and deserialize CDSCashBalance" in new Setup {
      val cdsCashBalance: CDSCashBalance = CDSCashBalance(Some(ten))
      val json: JsValue                  = Json.toJson(cdsCashBalance)
      json.validate[CDSCashBalance].get mustBe cdsCashBalance
    }

    "serialize and deserialize CDSAccounts" in new Setup {
      val accountList: List[CDSAccount] = List(
        CashAccount("1", "owner", AccountStatusOpen, CDSCashBalance(Some(ten))),
        GeneralGuaranteeAccount("2", "owner", AccountStatusClosed, Some(GeneralGuaranteeBalance(ten, ten)))
      )
      val cdsAccounts: CDSAccounts      = CDSAccounts("EORI123", accountList)

      val json: JsValue = Json.toJson(cdsAccounts)
      json.validate[CDSAccounts].get mustBe cdsAccounts
    }
  }

  trait Setup {
    val ten                         = 10
    implicit val messages: Messages = messagesApi.preferred(fakeRequest())

    val cashAccount = CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))

    val dutyDeferment =
      DutyDefermentAccount("67890", "GB210987654321", AccountStatusOpen, DutyDefermentBalance(None, None, None, None))

    val generalGuarantee =
      GeneralGuaranteeAccount("54321", "GB000000000000", AccountStatusOpen, Some(GeneralGuaranteeBalance(50.00, 50.00)))
  }
}
