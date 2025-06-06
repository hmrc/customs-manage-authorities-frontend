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

package models.requests

import models.domain.{AccountType, AuthorisedUser, StandingAuthority}
import play.api.libs.json.{Format, Json, OFormat}

case class Accounts(cash: Option[String], dutyDeferments: Seq[String], guarantee: Option[String]) {
  def hasOnlyDutyDefermentsAccount: Boolean = cash.isEmpty && guarantee.isEmpty

  def isDutyDefermentsAccountEmpty: Boolean = dutyDeferments.isEmpty
}

object Accounts {

  implicit val accountsFormat: Format[Accounts] = Json.format[Accounts]

}

case class AddAuthorityRequest(
  accounts: Accounts,
  authority: StandingAuthority,
  authorisedUser: AuthorisedUser,
  editRequest: Boolean = false,
  ownerEori: String
)

object AddAuthorityRequest {
  implicit val addAuthorityRequestFormat: Format[AddAuthorityRequest] = Json.format[AddAuthorityRequest]
}

case class RevokeAuthorityRequest(
  accountNumber: String,
  accountType: AccountType,
  authorisedEori: String,
  authorisedUser: AuthorisedUser,
  ownerEori: String
)

object RevokeAuthorityRequest {
  implicit val revokeAuthorityRequestFormat: OFormat[RevokeAuthorityRequest] = Json.format[RevokeAuthorityRequest]
}

case class GrantAccountAuthorityRequest(payload: AddAuthorityRequest, ownerEori: String)
