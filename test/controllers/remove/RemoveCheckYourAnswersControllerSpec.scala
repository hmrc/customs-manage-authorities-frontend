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

package controllers.remove

import base.SpecBase
import connectors.{CustomsDataStoreConnector, CustomsFinancialsConnector}
import models.UserAnswers
import models.domain.*
import models.requests.RevokeAuthorityRequest
import org.mockito.ArgumentMatchers.{any, eq => eqMatcher}
import org.mockito.Mockito.{verify, when}
import org.mockito.{ArgumentMatchers, Mockito}
import org.scalatestplus.mockito.MockitoSugar
import pages.remove.RemoveAuthorisedUserPage
import play.api.inject.guice.GuiceableModule
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.api.{Application, inject}
import services.{AccountAndAuthority, AuthoritiesCacheService, NoAccount, NoAuthority}
import utils.StringUtils.emptyString
import utils.TestData.XI_EORI

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RemoveCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {

  "onPageLoad" should {

    "redirect to error page if no account present" in new Setup {
      val app: Application = applicationWithUserAnswersAndEori()

      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Left(NoAccount)))

      running(app) {
        val result = route(app, getRequest).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe technicalDifficultiesPage
      }
    }

    "redirect to page if no authority present" in new Setup {
      val app: Application = applicationWithUserAnswersAndEori()

      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Left(NoAuthority)))

      running(app) {
        val result = route(app, getRequest).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe technicalDifficultiesPage
      }
    }

    "redirect to view authority if authorised user not present" in new Setup {
      val app: Application = applicationWithUserAnswersAndEori()

      when(mockCustomsDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some(XI_EORI)))

      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthority))))

      running(app) {
        val result = route(app, getRequest).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.ViewAuthorityController.onPageLoad("a", "b").url
      }
    }

    "return OK on successful request" in new Setup {
      private val userAnswers =
        emptyUserAnswers.set(RemoveAuthorisedUserPage("a", "b"), authUser).get

      val app: Application = applicationWithUserAnswersAndEori(
        userAnswers,
        euEori
      )

      when(mockCustomsDataStoreConnector.getCompanyName(any())).thenReturn(Future.successful(Some("Tony Stark")))
      when(mockCustomsDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some(XI_EORI)))
      when(mockCustomsFinancialsConnector.revokeAccountAuthorities(any)(any)).thenReturn(Future.successful(true))

      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthority))))

      running(app) {
        val result = route(app, postRequest).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.RemoveConfirmationController.onPageLoad("a", "b").url)
      }
    }
  }

  "onSubmit" should {
    "redirect to error page when no account present" in new Setup {
      val app: Application = applicationWithUserAnswersAndEori()

      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Left(NoAccount)))

      running(app) {
        val result = route(app, postRequest).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe technicalDifficultiesPage
      }
    }

    "redirect to error page when no authority present" in new Setup {
      val app: Application = applicationWithUserAnswersAndEori()

      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Left(NoAuthority)))

      running(app) {
        val result = route(app, postRequest).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe technicalDifficultiesPage
      }
    }

    "redirect to error page if no authorised user present" in new Setup {
      val app: Application = applicationWithUserAnswersAndEori()

      when(mockCustomsDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some(XI_EORI)))

      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthority))))

      running(app) {
        val result = route(app, postRequest).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe technicalDifficultiesPage
      }
    }

    "redirect to error page if revoke failed" in new Setup {
      private val userAnswers = emptyUserAnswers.set(RemoveAuthorisedUserPage("a", "b"), authUser).get

      val app: Application =
        applicationWithUserAnswersAndEori(userAnswers)

      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthority))))

      when(mockCustomsFinancialsConnector.revokeAccountAuthorities(any())(any()))
        .thenReturn(Future.successful(false))

      when(mockCustomsDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some(XI_EORI)))

      running(app) {
        val result = route(app, postRequest).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe technicalDifficultiesPage
      }
    }

    "redirect to confirmation page when successful" in new Setup {
      private val userAnswers = emptyUserAnswers.set(RemoveAuthorisedUserPage("a", "b"), authUser).get

      val app: Application =
        applicationWithUserAnswersAndEori(userAnswers)

      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthority))))

      when(mockCustomsFinancialsConnector.revokeAccountAuthorities(any())(any()))
        .thenReturn(Future.successful(true))

      when(mockCustomsDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some(XI_EORI)))

      running(app) {
        val result = route(app, postRequest).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.remove.routes.RemoveConfirmationController.onPageLoad("a", "b").url
      }
    }

    "redirect to confirmation page when successful and authorised EORI is EU" in new Setup {
      private val userAnswers = emptyUserAnswers.set(RemoveAuthorisedUserPage("a", "b"), authUser).get

      val app: Application =
        applicationWithUserAnswersAndEori(
          userAnswers,
          euEori
        )

      when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthority))))

      when(mockCustomsFinancialsConnector.revokeAccountAuthorities(any())(any()))
        .thenReturn(Future.successful(true))

      when(mockCustomsDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some(XI_EORI)))

      running(app) {
        val result = route(app, postRequest).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.remove.routes.RemoveConfirmationController.onPageLoad("a", "b").url
        verify(mockCustomsFinancialsConnector, Mockito.times(1))
          .revokeAccountAuthorities(eqMatcher(euRevokeAuthorityRequest))(any)
      }
    }

    "redirect to confirmation page and use GB Eori as ownerEori when authorisedEori is XI Eori " +
      "and account type is CdsCashAccount" in new Setup {
        val userAnswers: UserAnswers =
          emptyUserAnswers.set(RemoveAuthorisedUserPage("a", "b"), authUser).get

        val app: Application = applicationWithUserAnswersAndEori(
          userAnswers,
          gbEori
        )

        when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
          .thenReturn(
            Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthorityWithXIEori)))
          )

        when(
          mockCustomsFinancialsConnector.revokeAccountAuthorities(eqMatcher(xiRevokeAuthorityRequestWithGbOwnerCA))(
            any()
          )
        )
          .thenReturn(Future.successful(true))

        when(mockCustomsDataStoreConnector.getXiEori(any)(any)).thenReturn(Future(Option(xiEori)))

        running(app) {
          val result = route(app, postRequest).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe
            controllers.remove.routes.RemoveConfirmationController.onPageLoad("a", "b").url

          verify(mockCustomsFinancialsConnector, Mockito.times(1)).revokeAccountAuthorities(any)(any)
        }
      }

    "redirect to confirmation page and use GB Eori as ownerEori when authorisedEori is XI Eori " +
      "and account type is CdsGeneralGuaranteeAccount" in new Setup {
        val userAnswers: UserAnswers =
          emptyUserAnswers.set(RemoveAuthorisedUserPage("a", "b"), authUser).get

        val app: Application = applicationWithUserAnswersAndEori(
          userAnswers,
          gbEori
        )

        when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
          .thenReturn(
            Future.successful(
              Right(AccountAndAuthority(cdsGuaranteeAccWithAuthoritiesWithId, standingAuthorityWithXIEori))
            )
          )

        when(
          mockCustomsFinancialsConnector.revokeAccountAuthorities(eqMatcher(xiRevokeAuthorityRequestWithGbOwnerGG))(
            any()
          )
        )
          .thenReturn(Future.successful(true))

        when(mockCustomsDataStoreConnector.getXiEori(any)(any)).thenReturn(Future(Option(xiEori)))

        running(app) {
          val result = route(app, postRequest).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe
            controllers.remove.routes.RemoveConfirmationController.onPageLoad("a", "b").url

          verify(mockCustomsFinancialsConnector, Mockito.times(1)).revokeAccountAuthorities(any)(any)
        }
      }

    "redirect to confirmation page and use XI Eori as ownerEori when authorisedEori is XI Eori " +
      "and account type is CdsDutyDefermentAccount" in new Setup {
        val userAnswers: UserAnswers =
          emptyUserAnswers.set(RemoveAuthorisedUserPage("a", "b"), authUser).get

        val app: Application = applicationBuilder(Some(userAnswers), gbEori)
          .overrides(
            inject.bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService),
            inject.bind[CustomsFinancialsConnector].toInstance(mockCustomsFinancialsConnector),
            inject.bind[CustomsDataStoreConnector].toInstance(mockCustomsDataStoreConnector)
          )
          .build()

        when(mockAuthoritiesCacheService.getAccountAndAuthority(any(), any(), any())(any()))
          .thenReturn(
            Future.successful(
              Right(AccountAndAuthority(cdsDDAccountsWithAuthoritiesWithId, standingAuthorityWithXIEori))
            )
          )

        when(
          mockCustomsFinancialsConnector.revokeAccountAuthorities(eqMatcher(xiRevokeAuthorityRequestWithXiOwner))(any())
        )
          .thenReturn(Future.successful(true))

        when(mockCustomsDataStoreConnector.getXiEori(any)(any)).thenReturn(Future(Option(xiEori)))

        running(app) {
          val result = route(app, postRequest).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe
            controllers.remove.routes.RemoveConfirmationController.onPageLoad("a", "b").url

          verify(mockCustomsFinancialsConnector, Mockito.times(1)).revokeAccountAuthorities(any)(any)
        }
      }
  }

  trait Setup {
    val mockAuthoritiesCacheService: AuthoritiesCacheService       = mock[AuthoritiesCacheService]
    val mockCustomsFinancialsConnector: CustomsFinancialsConnector = mock[CustomsFinancialsConnector]
    val mockCustomsDataStoreConnector: CustomsDataStoreConnector   = mock[CustomsDataStoreConnector]

    val startDate: LocalDate = LocalDate.parse("2020-03-01")
    val endDate: LocalDate   = LocalDate.parse("2020-04-01")

    val gbEori                   = "GB123456789012"
    val xiEori                   = "XI123456789012"
    val euEori                   = "DE123456789012"
    val eori                     = "EORI"
    val authUser: AuthorisedUser = AuthorisedUser("test", "test")
    val accountNumber            = "12345"

    val standingAuthority: StandingAuthority           = StandingAuthority("EORI", startDate, Some(endDate), viewBalance = false)
    val standingAuthorityWithXIEori: StandingAuthority =
      StandingAuthority(xiEori, startDate, Some(endDate), viewBalance = false)

    val euRevokeAuthorityRequest: RevokeAuthorityRequest = RevokeAuthorityRequest(
      accountNumber = accountNumber,
      accountType = CdsCashAccount,
      authorisedEori = eori,
      authorisedUser = authUser,
      ownerEori = euEori
    )

    val xiRevokeAuthorityRequestWithGbOwnerGG: RevokeAuthorityRequest = RevokeAuthorityRequest(
      accountNumber = accountNumber,
      accountType = CdsGeneralGuaranteeAccount,
      authorisedEori = xiEori,
      authorisedUser = authUser,
      ownerEori = gbEori
    )

    val xiRevokeAuthorityRequestWithGbOwnerCA: RevokeAuthorityRequest = RevokeAuthorityRequest(
      accountNumber = accountNumber,
      accountType = CdsCashAccount,
      authorisedEori = xiEori,
      authorisedUser = authUser,
      ownerEori = gbEori
    )

    val xiRevokeAuthorityRequestWithXiOwner: RevokeAuthorityRequest = RevokeAuthorityRequest(
      accountNumber = accountNumber,
      accountType = CdsDutyDefermentAccount,
      authorisedEori = xiEori,
      authorisedUser = authUser,
      ownerEori = xiEori
    )

    val accountsWithAuthoritiesWithId: AccountWithAuthoritiesWithId =
      AccountWithAuthoritiesWithId(CdsCashAccount, "12345", Some(AccountStatusOpen), Map("b" -> standingAuthority))

    val cdsGuaranteeAccWithAuthoritiesWithId: AccountWithAuthoritiesWithId =
      AccountWithAuthoritiesWithId(
        CdsGeneralGuaranteeAccount,
        "12345",
        Some(AccountStatusOpen),
        Map("b" -> standingAuthority)
      )

    val cdsDDAccountsWithAuthoritiesWithId: AccountWithAuthoritiesWithId =
      AccountWithAuthoritiesWithId(
        CdsDutyDefermentAccount,
        "12345",
        Some(AccountStatusOpen),
        Map("b" -> standingAuthority)
      )

    val technicalDifficultiesPage: String = controllers.routes.TechnicalDifficulties.onPageLoad.url

    val getRequest: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, controllers.remove.routes.RemoveCheckYourAnswers.onPageLoad("a", "b").url)

    val postRequest: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(POST, controllers.remove.routes.RemoveCheckYourAnswers.onSubmit("a", "b").url)

    def applicationWithUserAnswersAndEori(
      userAnswer: UserAnswers = emptyUserAnswers,
      requestEori: String = emptyString
    ): Application = {
      val moduleList: Seq[GuiceableModule] =
        Seq(
          inject.bind[AuthoritiesCacheService].toInstance(mockAuthoritiesCacheService),
          inject.bind[CustomsFinancialsConnector].toInstance(mockCustomsFinancialsConnector),
          inject.bind[CustomsDataStoreConnector].toInstance(mockCustomsDataStoreConnector)
        )
      applicationBuilder(Some(userAnswer), requestEori).overrides(moduleList: _*).build()
    }
  }
}
