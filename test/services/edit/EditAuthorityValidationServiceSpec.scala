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

package services.edit

import base.SpecBase
import models.domain.{
  AccountStatusOpen, AccountWithAuthoritiesWithId, AuthorisedUser, CDSAccount, CDSCashBalance, CashAccount,
  CdsCashAccount, CdsDutyDefermentAccount, CdsGeneralGuaranteeAccount, DutyDefermentAccount, DutyDefermentBalance,
  GeneralGuaranteeAccount, GeneralGuaranteeBalance, StandingAuthority, UnknownAccount
}
import models.requests.{Accounts, AddAuthorityRequest}
import models.{AuthorityStart, CompanyDetails, ShowBalance, UnknownAccountType, UserAnswers}
import org.mockito.Mockito.when
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.add.{AccountsPage, AuthorityStartPage, EoriNumberPage, ShowBalancePage}
import pages.edit.EditAuthorisedUserPage
import play.api.{Application, inject}
import play.api.test.Helpers.running

import java.time.LocalDate

class EditAuthorityValidationServiceSpec extends SpecBase {

  "validate" must {

    "return correct AddAuthorityRequest" when {

      "EditCheckYourAnswerValidationService returns StandingAuthority" in new SetUp {

        when(mockECYAService.validate(userAnswers, accountIdVal, authorityIdVal, authorisedEoriVal))
          .thenReturn(Option(standingAuthority))

        val application: Application = applicationBuilder()
          .overrides(
            inject.bind[EditCheckYourAnswersValidationService].toInstance(mockECYAService)
          )
          .build()

        val service: EditAuthorityValidationService = application.injector.instanceOf[EditAuthorityValidationService]

        running(application) {
          service.validate(userAnswers, accountIdVal, authorityIdVal, authorisedEoriVal, accAuthority) mustBe
            Right(
              AddAuthorityRequest(Accounts(Some(accountNumberVal), Seq.empty, None), standingAuthority, authUser, true)
            )

          service.validate(userAnswers, accountIdVal, authorityIdVal, authorisedEoriVal, accAuthority02) mustBe
            Right(AddAuthorityRequest(Accounts(None, Seq(accountNumberVal), None), standingAuthority, authUser, true))

          service.validate(userAnswers, accountIdVal, authorityIdVal, authorisedEoriVal, accAuthority03) mustBe
            Right(
              AddAuthorityRequest(Accounts(None, Seq.empty, Some(accountNumberVal)), standingAuthority, authUser, true)
            )

          service.validate(userAnswers, accountIdVal, authorityIdVal, authorisedEoriVal, accAuthority04) mustBe
            Left(UnknownAccountType)
        }
      }
    }

    "return ErrorResponse" when {
      "EditCheckYourAnswerValidationService returns None" in new SetUp {

        when(mockECYAService.validate(userAnswers, accountIdVal, authorityIdVal, authorisedEoriVal))
          .thenReturn(None)

        val application: Application = applicationBuilder()
          .overrides(
            inject.bind[EditCheckYourAnswersValidationService].toInstance(mockECYAService)
          )
          .build()

        val service: EditAuthorityValidationService = application.injector.instanceOf[EditAuthorityValidationService]

        running(application) {
          service.validate(userAnswers, accountIdVal, authorityIdVal, authorisedEoriVal, accAuthority) mustBe Left(
            UnknownAccountType
          )
        }
      }
    }
  }
}

trait SetUp {

  val accountIdVal      = "123";
  val authorityIdVal    = "1234567"
  val authorisedEoriVal = "GB098765432109"
  val accountNumberVal  = "12345"

  val mockECYAService: EditCheckYourAnswersValidationService = mock[EditCheckYourAnswersValidationService]

  private val cashAccount =
    CashAccount(accountNumberVal, "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))

  private val dutyDefermentBalance = DutyDefermentBalance(None, None, None, None)
  private val dutyDeferment        = DutyDefermentAccount("67890", "GB210987654321", AccountStatusOpen, dutyDefermentBalance)
  private val genGuaranteeBal      = GeneralGuaranteeBalance(50.00, 50.00)

  private val generalGuarantee =
    GeneralGuaranteeAccount("54321", "GB000000000000", AccountStatusOpen, Some(genGuaranteeBal))

  val selectedAccounts: List[CDSAccount] = List(cashAccount, dutyDeferment, generalGuarantee)

  private val year2023       = 2023
  private val monthOfTheYear = 6
  private val dayOfMonth     = 12
  private val startDate      = LocalDate.of(year2023, monthOfTheYear, dayOfMonth)

  val standingAuthority: StandingAuthority       = StandingAuthority("someEori", startDate, None, viewBalance = false)
  val accAuthority: AccountWithAuthoritiesWithId =
    AccountWithAuthoritiesWithId(
      CdsCashAccount,
      accountNumberVal,
      Some(AccountStatusOpen),
      Map("b" -> standingAuthority)
    )

  val accAuthority02: AccountWithAuthoritiesWithId =
    AccountWithAuthoritiesWithId(
      CdsDutyDefermentAccount,
      accountNumberVal,
      Some(AccountStatusOpen),
      Map("b" -> standingAuthority)
    )

  val accAuthority03: AccountWithAuthoritiesWithId =
    AccountWithAuthoritiesWithId(
      CdsGeneralGuaranteeAccount,
      accountNumberVal,
      Some(AccountStatusOpen),
      Map("b" -> standingAuthority)
    )

  val accAuthority04: AccountWithAuthoritiesWithId =
    AccountWithAuthoritiesWithId(
      UnknownAccount,
      accountNumberVal,
      Some(AccountStatusOpen),
      Map("b" -> standingAuthority)
    )

  val authUser: AuthorisedUser = AuthorisedUser("test", "test2")

  val userAnswers: UserAnswers = UserAnswers("id")
    .set(AccountsPage, selectedAccounts)
    .success
    .value
    .set(EoriNumberPage, CompanyDetails("GB123456789012", Some("companyName")))
    .success
    .value
    .set(AuthorityStartPage, AuthorityStart.Today)(AuthorityStart.writes)
    .success
    .value
    .set(ShowBalancePage, ShowBalance.Yes)(ShowBalance.writes)
    .success
    .value
    .set(EditAuthorisedUserPage(accountIdVal, authorityIdVal), authUser)
    .success
    .value
}
