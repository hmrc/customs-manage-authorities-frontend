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

package services

import base.SpecBase
import models._
import models.domain._
import models.requests.Accounts
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.add._
import play.api.libs.json.{JsString, Writes}
import services.add.CheckYourAnswersValidationService
import utils.StringUtils.emptyString

import java.time.{LocalDate, LocalDateTime}

class CheckYourAnswersValidationServiceSpec extends SpecBase {

  "validate" must {

    "validate complete submission when authorityStart is today and authorityEnd is indefinite" in new Setup {
      implicit val writes: Writes[LocalDate] = (o: LocalDate) => JsString(o.toString)

      val currentDate = LocalDate.now()

      val userAnswers = completeUserAnswers
        .set(ShowBalancePage, ShowBalance.No)(ShowBalance.writes)
        .success
        .value
        .set(AuthorityStartPage, AuthorityStart.Today)(AuthorityStart.writes)
        .success
        .value
        .set(AuthorityEndPage, AuthorityEnd.Indefinite)(AuthorityEnd.writes)
        .success
        .value
        .set(AuthorityStartDatePage, currentDate)
        .success
        .value
        .set(AuthorityEndDatePage, currentDate.plusDays(1))
        .success
        .value
        .set(AuthorityDetailsPage, AuthorisedUser(emptyString, emptyString))
        .success
        .value

      val accounts          = Accounts(Some(cashAccount.number), Seq(dutyDeferment.number), Some(generalGuarantee.number))
      val standingAuthority =
        StandingAuthority("GB123456789012", LocalDate.now(), None, viewBalance = false)

      service.validate(userAnswers).value mustEqual (
        accounts,
        standingAuthority,
        AuthorisedUser(emptyString, emptyString)
      )
    }

    "validate should return None when authorityEndDateLage is null and AuthorityEnd is Setdate" in new Setup {
      implicit val writes: Writes[LocalDate] = (o: LocalDate) => JsString(o.toString)

      val currentDate = LocalDate.now()

      val userAnswers = completeUserAnswers
        .set(ShowBalancePage, ShowBalance.No)(ShowBalance.writes)
        .success
        .value
        .set(AuthorityStartPage, AuthorityStart.Today)(AuthorityStart.writes)
        .success
        .value
        .set(AuthorityEndPage, AuthorityEnd.Setdate)(AuthorityEnd.writes)
        .success
        .value
        .set(AuthorityStartDatePage, currentDate)
        .success
        .value
        .set(AuthorityDetailsPage, AuthorisedUser(emptyString, emptyString))
        .success
        .value

      service.validate(userAnswers) mustBe None
    }

    "validate complete submission from today to set date and show balance no" in new Setup {
      implicit val writes: Writes[LocalDate] = (o: LocalDate) => JsString(o.toString)

      val currentDate = LocalDate.now()

      val userAnswers = completeUserAnswers
        .set(ShowBalancePage, ShowBalance.No)(ShowBalance.writes)
        .success
        .value
        .set(AuthorityStartPage, AuthorityStart.Setdate)(AuthorityStart.writes)
        .success
        .value
        .set(AuthorityEndPage, AuthorityEnd.Setdate)(AuthorityEnd.writes)
        .success
        .value
        .set(AuthorityStartDatePage, currentDate)
        .success
        .value
        .set(AuthorityEndDatePage, currentDate.plusDays(1))
        .success
        .value
        .set(AuthorityDetailsPage, AuthorisedUser(emptyString, emptyString))
        .success
        .value

      val accounts          = Accounts(Some(cashAccount.number), Seq(dutyDeferment.number), Some(generalGuarantee.number))
      val standingAuthority =
        StandingAuthority("GB123456789012", LocalDate.now(), Option(currentDate.plusDays(1)), viewBalance = false)

      service.validate(userAnswers).value mustEqual (
        accounts,
        standingAuthority,
        AuthorisedUser(emptyString, emptyString)
      )
    }

    "reject submission missing Accounts" in new Setup {
      val userAnswers = completeUserAnswers
        .remove(AccountsPage)
        .success
        .value
      service.validate(userAnswers) mustBe None
    }

    "reject submission missing Eori number" in new Setup {
      val userAnswers = completeUserAnswers
        .remove(EoriNumberPage)
        .success
        .value
      service.validate(userAnswers) mustBe None
    }

    "reject submission missing Authority start" in new Setup {
      val userAnswers = completeUserAnswers
        .remove(AuthorityStartPage)
        .success
        .value
      service.validate(userAnswers) mustBe None
    }

    "reject submission missing Authority start date when set date is chosen" in new Setup {
      val userAnswers = completeUserAnswers
        .set(AuthorityStartPage, AuthorityStart.Setdate)
        .success
        .value
        .remove(AuthorityStartDatePage)
        .success
        .value
      service.validate(userAnswers) mustBe None
    }

    "reject submission missing Show balance" in new Setup {
      val userAnswers = completeUserAnswers
        .remove(ShowBalancePage)
        .success
        .value
      service.validate(userAnswers) mustBe None
    }

    /* "validate complete submission from today to indefinite" in {
              val accounts = Accounts(Some(cashAccount.number), Seq(dutyDeferment.number), Some(generalGuarantee.number))
              val standingAuthority = StandingAuthority("GB123456789012", LocalDate.now(), Option(LocalDate.now().plusDays(1)), viewBalance = true)
              val authorisedUser = AuthorisedUser("username", "role")

              service.validate(completeUserAnswers).value mustEqual Tuple3(accounts, standingAuthority, authorisedUser)
            }*/

    /*"validate complete submission with only one account selected" in {
      val userAnswer = completeUserAnswers
        .set(AccountsPage, List(generalGuarantee)).success.value

      val accounts = Accounts(None, Seq(), Some(generalGuarantee.number))
      val standingAuthority = StandingAuthority("GB123456789012", LocalDate.now(), Option(LocalDate.now().plusDays(1)), viewBalance = true)
      service.validate(userAnswer).value mustEqual Tuple2(accounts, standingAuthority)
    }
     */
    /* "validate complete submission with only one account" in {
      val userAnswer = completeUserAnswers
        .set(AccountsPage, List(dutyDeferment)).success.value

      val accounts = Accounts(None, Seq(dutyDeferment.number), None)
      val standingAuthority = StandingAuthority("GB123456789012", LocalDate.now(), Option(LocalDate.now().plusDays(1)), viewBalance = true)
      service.validate(userAnswer).value mustEqual Tuple2(accounts, standingAuthority)
    }*/

    /* "validate complete submission from set date to set date" in {
      implicit val writes: Writes[LocalDate] = (o: LocalDate) => JsString(o.toString)

      val startDate = LocalDate.now().plusMonths(6)
      val endDate = LocalDate.now().plusYears(1)

      val userAnswers = completeUserAnswers
        .set(AuthorityStartPage, AuthorityStart.Setdate).success.value
        .set(AuthorityStartDatePage, startDate).success.value

      val accounts = Accounts(Some(cashAccount.number), Seq(dutyDeferment.number), Some(generalGuarantee.number))
      val standingAuthority = StandingAuthority("GB123456789012", startDate, Some(endDate), viewBalance = true)
      service.validate(userAnswers).value mustEqual Tuple2(accounts, standingAuthority)
    }*/

    /*"validate complete submission from set date to indefinite" in {
      implicit val writes: Writes[LocalDate] = (o: LocalDate) => JsString(o.toString)

      val startDate = LocalDate.now().plusMonths(6)
      val endDate: LocalDate = LocalDate.parse("2020-04-01")

      val userAnswers = completeUserAnswers
        .set(AuthorityStartPage, AuthorityStart.Setdate).success.value
        .set(AuthorityStartDatePage, startDate).success.value

      val accounts = Accounts(Some(cashAccount.number), Seq(dutyDeferment.number), Some(generalGuarantee.number))
      val standingAuthority = StandingAuthority("GB123456789012", startDate, Some(endDate), viewBalance = true)
      service.validate(userAnswers).value mustEqual Tuple2(accounts, standingAuthority)
    }
     */
  }

  trait Setup {

    val mockDateTimeService: DateTimeService = mock[DateTimeService]

    val service = new CheckYourAnswersValidationService(mockDateTimeService)

    val cashAccount: CashAccount =
      CashAccount("12345", "GB123456789012", AccountStatusOpen, CDSCashBalance(Some(100.00)))

    val dutyDeferment: DutyDefermentAccount =
      DutyDefermentAccount("67890", "GB210987654321", AccountStatusOpen, DutyDefermentBalance(None, None, None, None))

    val generalGuarantee: GeneralGuaranteeAccount =
      GeneralGuaranteeAccount("54321", "GB000000000000", AccountStatusOpen, Some(GeneralGuaranteeBalance(50.00, 50.00)))

    val selectedAccounts: List[CDSAccount] = List(cashAccount, dutyDeferment, generalGuarantee)

    val completeUserAnswers: UserAnswers = UserAnswers("id")
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

    when(mockDateTimeService.localTime()).thenReturn(LocalDateTime.now())
  }
}
