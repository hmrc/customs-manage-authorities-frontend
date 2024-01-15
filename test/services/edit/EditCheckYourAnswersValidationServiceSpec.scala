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

package services.edit

import base.SpecBase
import models.domain.{
  AccountStatusOpen, AccountWithAuthoritiesWithId, AuthorisedUser, CDSAccount,
  CDSCashBalance, CashAccount, CdsCashAccount, DutyDefermentAccount, DutyDefermentBalance,
  GeneralGuaranteeAccount, GeneralGuaranteeBalance, StandingAuthority
}
import models.{AuthorityEnd, AuthorityStart, CompanyDetails, ShowBalance, UserAnswers}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.add.{AccountsPage, EoriNumberPage}
import pages.edit._
import play.api.{Application, inject}
import play.api.test.Helpers.running
import services.DateTimeService

import java.time.{LocalDate, LocalDateTime}

class EditCheckYourAnswersValidationServiceSpec extends SpecBase {
  "validate" must {
    "return StandingAuthority" when {
      "EditAuthorityEndPage has Setdate" in new SetUp {
        when(mockDateTimeService.localTime()).thenReturn(
          LocalDateTime.of(yearVal, monthVal, dayOfMonthVal, hourVal, minuteVal))

        val application: Application = applicationBuilder().overrides(
          inject.bind[DateTimeService].toInstance(mockDateTimeService)
        ).build()

        val service: EditCheckYourAnswersValidationService =
          application.injector.instanceOf[EditCheckYourAnswersValidationService]

        running(application) {
          service.validate(userAnswers,
            accountIdVal, authorityIdVal, eoriVal) mustBe Option(StandingAuthority(
            eoriVal,
            LocalDate.of(yearVal, monthVal, dayOfMonthVal),
            Option(LocalDate.of(yearVal, monthVal, dayOfMonthVal)),
            viewBalance = true))
        }
      }

      "EditAuthorityStartPage has Setdate" in new SetUp {
        when(mockDateTimeService.localTime()).thenReturn(
          LocalDateTime.of(yearVal, monthVal, dayOfMonthVal, hourVal, minuteVal))

        val tempUserAnswers = userAnswers
          .remove(EditAuthorityStartPage(accountIdVal, authorityIdVal)).success.value
          .set(EditAuthorityStartPage(accountIdVal, authorityIdVal), AuthorityStart.Setdate)(AuthorityStart.writes).success.value
          .remove(EditAuthorityEndDatePage(accountIdVal, authorityIdVal)).success.value
          .set(EditAuthorityStartDatePage(accountIdVal, authorityIdVal),
            LocalDate.of(yearVal, monthVal, dayOfMonthVal)).success.value

        val application: Application = applicationBuilder().overrides(
          inject.bind[DateTimeService].toInstance(mockDateTimeService)
        ).build()

        val service: EditCheckYourAnswersValidationService =
          application.injector.instanceOf[EditCheckYourAnswersValidationService]

        running(application) {
          service.validate(tempUserAnswers, accountIdVal, authorityIdVal,
            eoriVal) mustBe None
        }
      }
    }

    "return None" when {
      "EditAuthorityEndPage is set to Indefinite and ShowBalance is not set in UserAnswers" in new SetUp {
        when(mockDateTimeService.localTime()).thenReturn(
          LocalDateTime.of(yearVal, monthVal, dayOfMonthVal, hourVal, minuteVal))

        val application: Application = applicationBuilder().overrides(
          inject.bind[DateTimeService].toInstance(mockDateTimeService)
        ).build()

        val service: EditCheckYourAnswersValidationService =
          application.injector.instanceOf[EditCheckYourAnswersValidationService]

        running(application) {
          service.validate(userAnswersWithAuthorityEndIndefinite,
            accountIdVal, authorityIdVal, eoriVal) mustBe empty
        }
      }
    }

    "return None" when {
      "RunTimeException is raised" in new SetUp {
        when(mockDateTimeService.localTime()).thenThrow(new IndexOutOfBoundsException("Error Message"))

        val application: Application = applicationBuilder().overrides(
          inject.bind[DateTimeService].toInstance(mockDateTimeService)
        ).build()

        val service: EditCheckYourAnswersValidationService =
          application.injector.instanceOf[EditCheckYourAnswersValidationService]

        running(application) {
          service.validate(userAnswersWithAuthorityEndIndefinite,
            accountIdVal, authorityIdVal, eoriVal) mustBe empty
        }
      }
    }
  }

  trait SetUp {

    val accountIdVal = "123"
    val authorityIdVal = "1234567"
    val eoriVal = "GB123456789012"
    val yearVal = 2023
    val monthVal = 6
    val dayOfMonthVal = 12
    val hourVal = 10
    val minuteVal = 12

    val mockDateTimeService = mock[DateTimeService]
    val cashAccount = CashAccount("12345", eoriVal, AccountStatusOpen, CDSCashBalance(Some(100.00)))
    val dutyDefermentBalance = DutyDefermentBalance(None, None, None, None)
    val dutyDeferment = DutyDefermentAccount("67890", "GB210987654321", AccountStatusOpen, dutyDefermentBalance)
    val genGuaranteeBal = GeneralGuaranteeBalance(50.00, 50.00)
    val generalGuarantee = GeneralGuaranteeAccount("54321", "GB000000000000", AccountStatusOpen, Some(genGuaranteeBal))
    val selectedAccounts: List[CDSAccount] = List(cashAccount, dutyDeferment, generalGuarantee)
    val startDate = LocalDate.of(yearVal, monthVal, dayOfMonthVal)
    val standingAuthority = StandingAuthority("someEori", startDate, None, viewBalance = false)

    val accAuthority = AccountWithAuthoritiesWithId(CdsCashAccount, "12345", Some(AccountStatusOpen),
      Map("b" -> standingAuthority))
    val authUser: AuthorisedUser = AuthorisedUser("test", "test2")

    val userAnswers: UserAnswers = UserAnswers("id")
      .set(AccountsPage, selectedAccounts).success.value
      .set(EoriNumberPage, CompanyDetails(eoriVal, Some("companyName"))).success.value
      .set(EditAuthorityStartPage(accountIdVal, authorityIdVal), AuthorityStart.Today)(AuthorityStart.writes).success.value
      .set(EditAuthorityEndPage(accountIdVal, authorityIdVal), AuthorityEnd.Setdate)(AuthorityEnd.writes).success.value
      .set(EditAuthorityEndDatePage(accountIdVal, authorityIdVal), LocalDate.of(yearVal, monthVal, dayOfMonthVal)).success.value
      .set(EditShowBalancePage(accountIdVal, authorityIdVal), ShowBalance.Yes)(ShowBalance.writes).success.value
      .set(EditAuthorisedUserPage(accountIdVal, authorityIdVal), authUser).success.value

    val userAnswersWithAuthorityEndIndefinite: UserAnswers = UserAnswers("id")
      .set(AccountsPage, selectedAccounts).success.value
      .set(EoriNumberPage, CompanyDetails(eoriVal, Some("companyName"))).success.value
      .set(EditAuthorityStartPage(accountIdVal, authorityIdVal), AuthorityStart.Today)(AuthorityStart.writes).success.value
      .set(EditAuthorityEndPage(accountIdVal, authorityIdVal), AuthorityEnd.Indefinite)(AuthorityEnd.writes).success.value
      .set(EditAuthorityEndDatePage(accountIdVal, authorityIdVal), LocalDate.of(yearVal, monthVal, dayOfMonthVal)).success.value
      .set(EditAuthorisedUserPage(accountIdVal, authorityIdVal), authUser).success.value
  }
}
