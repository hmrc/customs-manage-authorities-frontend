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

package models.domain

import play.api.Logger
import play.api.libs.json._

sealed trait AccountType

case object CdsCashAccount extends AccountType
case object CdsDutyDefermentAccount extends AccountType
case object CdsGeneralGuaranteeAccount extends AccountType
case object UnknownAccount extends AccountType

object AccountType {
  val logger: Logger = Logger(this.getClass)

  implicit val AccountTypeReads: Reads[AccountType] = (json: JsValue) => {
    json.as[String] match {
      case "CDSCash" => JsSuccess(CdsCashAccount)
      case "DutyDeferment" => JsSuccess(CdsDutyDefermentAccount)
      case "GeneralGuarantee" => JsSuccess(CdsGeneralGuaranteeAccount)
      case unknownAccountType =>
        logger.warn(s"Unknown account type received: $unknownAccountType")
        JsSuccess(UnknownAccount)
    }
  }

  implicit val accountTypeWrites: Writes[AccountType] = (obj: AccountType) => JsString(
    obj match {
      case CdsCashAccount => "CDSCash"
      case CdsDutyDefermentAccount => "DutyDeferment"
      case CdsGeneralGuaranteeAccount => "GeneralGuarantee"
      case UnknownAccount => "UnknownAccount"
    }
  )

}