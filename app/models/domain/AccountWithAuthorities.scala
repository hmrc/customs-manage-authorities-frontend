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

import play.api.libs.json.{Format, Json, Reads}

import java.util.{Base64, UUID}

case class AccountWithAuthorities(accountType: AccountType,
                                  accountNumber: AccountNumber,
                                  accountStatus: Option[CDSAccountStatus],
                                  authorities: Seq[StandingAuthority])

object AccountWithAuthorities {
  implicit val accountWithAuthoritiesReads: Reads[AccountWithAuthorities] = Json.reads[AccountWithAuthorities]
}

case class AccountWithAuthoritiesWithId(accountType: AccountType,
                                        accountNumber: AccountNumber,
                                        accountStatus: Option[CDSAccountStatus],
                                        authorities: Map[String, StandingAuthority])

object AccountWithAuthoritiesWithId {

  implicit val accountWithAuthoritiesWithIdFormat: Format[AccountWithAuthoritiesWithId] =
    Json.format[AccountWithAuthoritiesWithId]

  private val encoder = Base64.getUrlEncoder

  def apply(accountWithAuthorities: AccountWithAuthorities): AccountWithAuthoritiesWithId = {
    val authoritiesWithId =
      accountWithAuthorities.authorities.map(new String(encoder.encode(UUID.randomUUID().toString.getBytes)) -> _).toMap

    AccountWithAuthoritiesWithId(
      accountWithAuthorities.accountType,
      accountWithAuthorities.accountNumber,
      accountWithAuthorities.accountStatus,
      authoritiesWithId
    )
  }

}

case class AuthoritiesWithId(authorities: Map[String, AccountWithAuthoritiesWithId]) {
  def accounts: Seq[AccountWithAuthoritiesWithId] = authorities.values.toSeq

  def authorisedWithEori(eori: EORI): Seq[AccountWithAuthoritiesWithId] =
    accounts.filter(_.authorities.values.exists(_.containsEori(eori)))

  def uniqueAuthorisedEORIs: Set[EORI] = {
    accounts.flatMap(_.authorities.values).map(_.authorisedEori).toSet
  }
}

object AuthoritiesWithId {

  implicit val authoritiesWithIdFormat: Format[AuthoritiesWithId] = Json.format[AuthoritiesWithId]

  private val encoder = Base64.getUrlEncoder

  def apply(authorities: Seq[AccountWithAuthorities]): AuthoritiesWithId = {
    AuthoritiesWithId(authorities.map { account =>
      new String(encoder.encode(UUID.randomUUID().toString.getBytes)) -> AccountWithAuthoritiesWithId(account)
    }.toMap)
  }
}
