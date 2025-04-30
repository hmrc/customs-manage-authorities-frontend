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

package services.add

import base.SpecBase
import models.domain.{
  AccountStatusOpen, AuthorisedUser, CDSAccount, CDSCashBalance, CashAccount, DutyDefermentAccount,
  DutyDefermentBalance, GeneralGuaranteeAccount, GeneralGuaranteeBalance, StandingAuthority
}
import models.requests.{Accounts, AddAuthorityRequest}
import models.{AuthorityStart, CompanyDetails, ShowBalance, UserAnswers}
import org.mockito.Mockito.when
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.add.{AccountsPage, AuthorityStartPage, EoriNumberPage, ShowBalancePage}
import pages.edit.EditAuthorisedUserPage
import play.api.{Application, inject}
import play.api.test.Helpers.running

import java.time.LocalDate

class AddAuthorityValidationServiceSpec extends SpecBase {

  "validate" must {

    "return correct AddAuthorityRequest" when {

      "CheckYourAnswersValidationService returns some value" in new SetUp {

        when(mockCYAService.validate(userAnswers))
          .thenReturn(Option((Accounts(Some("12345"), Seq.empty, None), standingAuthority, authUser)))

        val application: Application = applicationBuilder()
          .overrides(
            inject.bind[CheckYourAnswersValidationService].toInstance(mockCYAService)
          )
          .build()

        val service: AddAuthorityValidationService = application.injector.instanceOf[AddAuthorityValidationService]

        running(application) {
          service.validate(userAnswers, ownerEori) mustBe Option(
            AddAuthorityRequest(
              Accounts(Some("12345"), Seq.empty, None),
              standingAuthority,
              authUser,
              editRequest = false,
              ownerEori
            )
          )
        }

      }
    }

    "return None" when {

      "CheckYourAnswersValidationService returns no value" in new SetUp {
        when(mockCYAService.validate(userAnswers)).thenReturn(None)

        val application: Application = applicationBuilder()
          .overrides(
            inject.bind[CheckYourAnswersValidationService].toInstance(mockCYAService)
          )
          .build()

        val service: AddAuthorityValidationService = application.injector.instanceOf[AddAuthorityValidationService]

        running(application) {
          service.validate(userAnswers, ownerEori) mustBe empty
        }
      }
    }
  }
}

trait SetUp {
  val mockCYAService: CheckYourAnswersValidationService = mock[CheckYourAnswersValidationService]

  private val cashAccount          = CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))
  private val dutyDefermentBalance = DutyDefermentBalance(None, None, None, None)
  private val dutyDeferment        = DutyDefermentAccount("67890", "GB210987654321", AccountStatusOpen, dutyDefermentBalance)
  private val genGuaranteeBal      = GeneralGuaranteeBalance(50.00, 50.00)
  private val generalGuarantee     =
    GeneralGuaranteeAccount("54321", "GB000000000000", AccountStatusOpen, Some(genGuaranteeBal))

  val selectedAccounts: List[CDSAccount] = List(cashAccount, dutyDeferment, generalGuarantee)

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
    .set(EditAuthorisedUserPage("123", "1234567"), authUser)
    .success
    .value

  val ownerEori      = "GB12345"
  val year           = 2023
  val monthOfTheYear = 6
  val dayOfTheMonth  = 12

  val startDate: LocalDate                 = LocalDate.of(year, monthOfTheYear, dayOfTheMonth)
  val standingAuthority: StandingAuthority = StandingAuthority("someEori", startDate, None, viewBalance = false)
}
