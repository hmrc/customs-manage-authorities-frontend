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

package models.domain

import base.SpecBase
import play.api.libs.json.{JsSuccess, Json}

class AccountTypeSpec extends SpecBase {

  "AccountTypeReads" should {
    "output the correct value" in {
      import models.domain.AccountType.AccountTypeReads

      Json.fromJson(Json.parse("\"CDSCash\"")) mustBe JsSuccess(CdsCashAccount)
      Json.fromJson(Json.parse("\"DutyDeferment\"")) mustBe JsSuccess(CdsDutyDefermentAccount)
      Json.fromJson(Json.parse("\"GeneralGuarantee\"")) mustBe JsSuccess(CdsGeneralGuaranteeAccount)
      Json.fromJson(Json.parse("\"Invalid\"")) mustBe JsSuccess(UnknownAccount)
    }
  }

  "accountTypeWrites" should {
    "output the correct value" in {
      import models.domain.AccountType.accountTypeWrites

      Json.toJson(accountTypeWrites.writes(CdsCashAccount)) mustBe Json.parse("\"CDSCash\"")
      Json.toJson(accountTypeWrites.writes(CdsDutyDefermentAccount)) mustBe Json.parse("\"DutyDeferment\"")
      Json.toJson(accountTypeWrites.writes(CdsGeneralGuaranteeAccount)) mustBe Json.parse("\"GeneralGuarantee\"")
      Json.toJson(accountTypeWrites.writes(UnknownAccount)) mustBe Json.parse("\"UnknownAccount\"")
    }
  }
}
