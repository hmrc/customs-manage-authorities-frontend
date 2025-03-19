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

package controllers.actions

import base.SpecBase
import models.domain.{AccountStatusOpen, CDSCashBalance, CashAccount}
import models.requests.DataRequest
import models.{AuthorisedAccounts, CompanyDetails, InternalId, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.{AccountsPage, EoriNumberPage}
import play.api.http.HttpEntity
import play.api.http.Status.SEE_OTHER
import play.api.mvc.{AnyContentAsEmpty, ResponseHeader, Result}
import services.AuthorisedAccountsService
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.retrieve.Credentials
import utils.StringUtils.emptyString

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VerifyAccountNumbersActionSpec extends SpecBase with MockitoSugar {

  "Verify Account Numbers Action" should {

    "redirect the user to the EORI entry page if the eori is not present in the request" in new Setup {
      val action = new Harness()

      val userAnswersWithEori: UserAnswers =
        emptyUserAnswers
          .set(AccountsPage, List.empty)
          .success
          .value

      private val futureResult = action.callRefine(dataRequest(userAnswersWithEori))

      whenReady(futureResult) { result =>
        result mustBe
          Left(
            Result(
              ResponseHeader(SEE_OTHER, Map("Location" -> "/customs/manage-authorities/add-authority/eori-number")),
              HttpEntity.NoEntity
            )
          )
      }
    }

    "redirect the user to the Accounts entry page if the accounts aren't present in the request" in new Setup {
      val action                           = new Harness()
      val userAnswersAccounts: UserAnswers =
        emptyUserAnswers
          .set(EoriNumberPage, CompanyDetails("someEori", Some("1")))
          .success
          .value

      private val futureResult = action.callRefine(dataRequest(userAnswersAccounts))

      whenReady(futureResult) { result =>
        result mustBe
          Left(
            Result(
              ResponseHeader(SEE_OTHER, Map("Location" -> "/customs/manage-authorities/add-authority/accounts")),
              HttpEntity.NoEntity
            )
          )
      }
    }

    "redirect the user to the Accounts page if there is an account number that" +
      " can't be authorised due to existing authority" in new Setup {
        val action    = new Harness()
        val amount100 = 100

        private val accounts = List(CashAccount("1234", "4321", AccountStatusOpen, CDSCashBalance(Some(amount100))))

        val userAnswersWithEoriAndAccounts: UserAnswers =
          emptyUserAnswers
            .set(EoriNumberPage, CompanyDetails("someEori", Some("1")))
            .success
            .value
            .set(AccountsPage, accounts)
            .success
            .value

        when(mockAuthorisedAccountService.getAuthorisedAccounts(any())(any(), any()))
          .thenReturn(Future.successful(AuthorisedAccounts(accounts, Seq.empty, Seq.empty, Seq.empty, "someEori")))

        private val futureResult = action.callRefine(dataRequest(userAnswersWithEoriAndAccounts))

        whenReady(futureResult) { result =>
          result mustBe
            Left(
              Result(
                ResponseHeader(SEE_OTHER, Map("Location" -> "/customs/manage-authorities/add-authority/accounts")),
                HttpEntity.NoEntity
              )
            )
        }

      }

    "return the request if the account numbers are valid" in new Setup {
      val action = new Harness()

      val userAnswersWithEoriAndAccounts: UserAnswers =
        emptyUserAnswers
          .set(EoriNumberPage, CompanyDetails("someEori", Some("1")))
          .success
          .value
          .set(AccountsPage, List.empty)
          .success
          .value

      private val futureResult = action.callRefine(dataRequest(userAnswersWithEoriAndAccounts))

      whenReady(futureResult) {
        case Left(_)  => fail()
        case Right(v) => v.userAnswers mustBe userAnswersWithEoriAndAccounts
      }
    }
  }

  trait Setup {
    val mockAuthorisedAccountService: AuthorisedAccountsService = mock[AuthorisedAccountsService]

    when(mockAuthorisedAccountService.getAuthorisedAccounts(any())(any(), any()))
      .thenReturn(
        Future.successful(
          AuthorisedAccounts(Seq.empty, Seq.empty, Seq.empty, Seq.empty, "someEori")
        )
      )

    def dataRequest(userAnswers: UserAnswers): DataRequest[AnyContentAsEmpty.type] = DataRequest(
      fakeRequest(),
      InternalId("id"),
      Credentials(emptyString, emptyString),
      Organisation,
      Some("email"),
      "eori",
      userAnswers
    )

    class Harness extends VerifyAccountNumbersActionImpl(mockAuthorisedAccountService) {
      def callRefine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
    }
  }
}
