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

package utils

import models.domain.{AccountStatusClosed, AccountStatusOpen, AccountStatusPending, AccountStatusSuspended, AccountWithAuthoritiesWithId, AuthoritiesWithId, CdsCashAccount, CdsDutyDefermentAccount, CdsGeneralGuaranteeAccount, StandingAuthority}
import uk.gov.hmrc.auth.core.retrieve.Email

import java.time.LocalDate

object TestData {

  val ACCOUNT_ID = "a"
  val AUTH_ID_B = "b"
  val AUTH_ID_C = "c"

  val ACCOUNT_NUMBER = "12345678"
  val EORI_NUMBER = "EORI"
  val XI_EORI = "XI12345678"

  val testEmail: Email = Email("test_address")

  val START_DATE_1: LocalDate = LocalDate.parse("2020-03-01")
  val START_DATE_2: LocalDate = LocalDate.parse("2020-04-01")
  val END_DATE_1: LocalDate = LocalDate.parse("2020-04-01")
  val END_DATE_2: LocalDate = LocalDate.parse("2020-05-01")

  val STANDING_AUTHORITY_1: StandingAuthority =
    StandingAuthority(EORI_NUMBER, START_DATE_1, Some(END_DATE_1), viewBalance = false)

  val STANDING_AUTHORITY_2: StandingAuthority =
    StandingAuthority(EORI_NUMBER, START_DATE_2, Some(END_DATE_2), viewBalance = false)

  val STANDING_AUTHORITY_3: StandingAuthority =
    StandingAuthority(EORI_NUMBER, START_DATE_1, Some(END_DATE_1), viewBalance = true)

  val OPEN_CASH_ACC_WITH_AUTH_WITH_ID: AccountWithAuthoritiesWithId = AccountWithAuthoritiesWithId(
    CdsCashAccount,
    ACCOUNT_NUMBER,
    Some(AccountStatusOpen),
    Map(AUTH_ID_B -> STANDING_AUTHORITY_1, AUTH_ID_C -> STANDING_AUTHORITY_2))

  val CLOSED_CASH_ACC_WITH_AUTH_WITH_ID: AccountWithAuthoritiesWithId = AccountWithAuthoritiesWithId(
    CdsCashAccount,
    ACCOUNT_NUMBER,
    Some(AccountStatusClosed),
    Map(AUTH_ID_B -> STANDING_AUTHORITY_1, AUTH_ID_C -> STANDING_AUTHORITY_2))

  val PENDING_CASH_ACC_WITH_AUTH_WITH_ID: AccountWithAuthoritiesWithId = AccountWithAuthoritiesWithId(
    CdsCashAccount,
    ACCOUNT_NUMBER,
    Some(AccountStatusPending),
    Map(AUTH_ID_B -> STANDING_AUTHORITY_1, AUTH_ID_C -> STANDING_AUTHORITY_2))

  val SUSPENDED_CASH_ACC_WITH_AUTH_WITH_ID: AccountWithAuthoritiesWithId = AccountWithAuthoritiesWithId(
    CdsCashAccount,
    ACCOUNT_NUMBER,
    Some(AccountStatusSuspended),
    Map(AUTH_ID_B -> STANDING_AUTHORITY_1, AUTH_ID_C -> STANDING_AUTHORITY_2))

  val OPEN_DD_ACC_WITH_AUTH_WITH_ID: AccountWithAuthoritiesWithId = AccountWithAuthoritiesWithId(
    CdsDutyDefermentAccount,
    ACCOUNT_NUMBER,
    Some(AccountStatusOpen),
    Map(AUTH_ID_B -> STANDING_AUTHORITY_1, AUTH_ID_C -> STANDING_AUTHORITY_2))

  val NONE_DD_ACC_WITH_AUTH_WITH_ID: AccountWithAuthoritiesWithId = AccountWithAuthoritiesWithId(
    CdsDutyDefermentAccount,
    ACCOUNT_NUMBER,
    None,
    Map(AUTH_ID_B -> STANDING_AUTHORITY_1, AUTH_ID_C -> STANDING_AUTHORITY_2))

  val OPEN_GG_ACC_WITH_AUTH_WITH_ID: AccountWithAuthoritiesWithId = AccountWithAuthoritiesWithId(
    CdsGeneralGuaranteeAccount,
    ACCOUNT_NUMBER,
    Some(AccountStatusOpen),
    Map(AUTH_ID_B -> STANDING_AUTHORITY_1, AUTH_ID_C -> STANDING_AUTHORITY_2))

  val CLOSED_GG_ACC_WITH_AUTH_WITH_ID: AccountWithAuthoritiesWithId = AccountWithAuthoritiesWithId(
    CdsGeneralGuaranteeAccount,
    ACCOUNT_NUMBER,
    Some(AccountStatusClosed),
    Map(AUTH_ID_B -> STANDING_AUTHORITY_1, AUTH_ID_C -> STANDING_AUTHORITY_3))

  val authoritiesWithId: AuthoritiesWithId = AuthoritiesWithId(Map(
    ("a" ->
      AccountWithAuthoritiesWithId(
        CdsCashAccount,
        ACCOUNT_NUMBER,
        Some(AccountStatusOpen),
        Map(AUTH_ID_B -> STANDING_AUTHORITY_1, AUTH_ID_C -> STANDING_AUTHORITY_2)))
  ))
}
