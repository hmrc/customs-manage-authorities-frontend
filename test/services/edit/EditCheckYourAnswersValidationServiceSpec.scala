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
          LocalDateTime.of(2023, 6, 12, 10, 12))

        val application: Application = applicationBuilder().overrides(
          inject.bind[DateTimeService].toInstance(mockDateTimeService)
        ).build()

        val service: EditCheckYourAnswersValidationService =
          application.injector.instanceOf[EditCheckYourAnswersValidationService]

        running(application) {
          service.validate(userAnswers,
            "123", "1234567", "GB123456789012") mustBe Option(StandingAuthority(
            "GB123456789012",
            LocalDate.of(2023, 6, 12),
            Option(LocalDate.of(2023, 6, 12)),
            viewBalance = true))
        }
      }

      "EditAuthorityStartPage has Setdate" in new SetUp {
        when(mockDateTimeService.localTime()).thenReturn(
          LocalDateTime.of(2023, 6, 12, 10, 12))

        val tempUserAnswers = userAnswers
          .remove(EditAuthorityStartPage("123", "1234567")).success.value
          .set(EditAuthorityStartPage("123", "1234567"), AuthorityStart.Setdate)(AuthorityStart.writes).success.value
          .remove(EditAuthorityEndDatePage("123", "1234567")).success.value
          .set(EditAuthorityStartDatePage("123", "1234567"),
            LocalDate.of(2023, 6, 12)).success.value

        val application: Application = applicationBuilder().overrides(
          inject.bind[DateTimeService].toInstance(mockDateTimeService)
        ).build()

        val service: EditCheckYourAnswersValidationService =
          application.injector.instanceOf[EditCheckYourAnswersValidationService]

        running(application) {
          service.validate(tempUserAnswers, "123", "1234567",
            "GB123456789012") mustBe None
        }
      }


    }

    "return None" when {
      "EditAuthorityEndPage is set to Indefinite and ShowBalance is not set in UserAnswers" in new SetUp {
        when(mockDateTimeService.localTime()).thenReturn(
          LocalDateTime.of(2023, 6, 12, 10, 12))

        val application: Application = applicationBuilder().overrides(
          inject.bind[DateTimeService].toInstance(mockDateTimeService)
        ).build()

        val service: EditCheckYourAnswersValidationService =
          application.injector.instanceOf[EditCheckYourAnswersValidationService]

        running(application) {
          service.validate(userAnswersWithAuthorityEndIndefinite,
            "123", "1234567", "GB123456789012") mustBe empty
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
            "123", "1234567", "GB123456789012") mustBe empty
        }
      }
    }
  }

  trait SetUp {
    val mockDateTimeService = mock[DateTimeService]
    val cashAccount = CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))
    val dutyDefermentBalance = DutyDefermentBalance(None, None, None, None)
    val dutyDeferment = DutyDefermentAccount("67890", "GB210987654321", AccountStatusOpen, dutyDefermentBalance)
    val genGuaranteeBal = GeneralGuaranteeBalance(50.00, 50.00)
    val generalGuarantee = GeneralGuaranteeAccount("54321", "GB000000000000", AccountStatusOpen, Some(genGuaranteeBal))
    val selectedAccounts: List[CDSAccount] = List(cashAccount, dutyDeferment, generalGuarantee)
    val startDate = LocalDate.of(2023, 6, 12)
    val standingAuthority = StandingAuthority("someEori", startDate, None, viewBalance = false)

    val accAuthority = AccountWithAuthoritiesWithId(CdsCashAccount, "12345", Some(AccountStatusOpen),
      Map("b" -> standingAuthority))
    val authUser: AuthorisedUser = AuthorisedUser("test", "test2")

    val userAnswers: UserAnswers = UserAnswers("id")
      .set(AccountsPage, selectedAccounts).success.value
      .set(EoriNumberPage, CompanyDetails("GB123456789012", Some("companyName"))).success.value
      .set(EditAuthorityStartPage("123", "1234567"), AuthorityStart.Today)(AuthorityStart.writes).success.value
      .set(EditAuthorityEndPage("123", "1234567"), AuthorityEnd.Setdate)(AuthorityEnd.writes).success.value
      .set(EditAuthorityEndDatePage("123", "1234567"), LocalDate.of(2023, 6, 12)).success.value
      .set(EditShowBalancePage("123", "1234567"), ShowBalance.Yes)(ShowBalance.writes).success.value
      .set(EditAuthorisedUserPage("123", "1234567"), authUser).success.value

    val userAnswersWithAuthorityEndIndefinite: UserAnswers = UserAnswers("id")
      .set(AccountsPage, selectedAccounts).success.value
      .set(EoriNumberPage, CompanyDetails("GB123456789012", Some("companyName"))).success.value
      .set(EditAuthorityStartPage("123", "1234567"), AuthorityStart.Today)(AuthorityStart.writes).success.value
      .set(EditAuthorityEndPage("123", "1234567"), AuthorityEnd.Indefinite)(AuthorityEnd.writes).success.value
      .set(EditAuthorityEndDatePage("123", "1234567"), LocalDate.of(2023, 6, 12)).success.value
      .set(EditAuthorisedUserPage("123", "1234567"), authUser).success.value
  }
}
