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

package controllers.edit

import base.SpecBase
import config.FrontendAppConfig
import connectors.{CustomsDataStoreConnector, CustomsFinancialsConnector}
import controllers.actions.{FakeVerifyAccountNumbersAction, VerifyAccountNumbersAction}
import models.AuthorityEnd.Indefinite
import models.AuthorityStart.Today
import models.ShowBalance.Yes
import models.domain.{
  AccountStatusOpen, AccountWithAuthorities, AccountWithAuthoritiesWithId, AuthorisedUser, AuthoritiesWithId,
  CdsCashAccount, StandingAuthority
}
import models.requests.{Accounts, AddAuthorityRequest}
import models.{AuthorityEnd, AuthorityStart, ShowBalance, UnknownAccountType, UserAnswers, domain, withNameToString}
import org.mockito.ArgumentMatchers.{any, eq => eqMatcher}
import org.mockito.Mockito.{verify, when}
import org.mockito.{ArgumentMatchers, Mockito}
import org.scalatestplus.mockito.MockitoSugar
import pages.edit.*
import play.api.mvc.Call
import play.api.test.Helpers.*
import play.api.{Application, inject}
import repositories.AuthoritiesRepository
import services.*
import services.add.CheckYourAnswersValidationService
import services.edit.EditAuthorityValidationService
import viewmodels.CheckYourAnswersEditHelper
import views.html.edit.EditCheckYourAnswersView

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Future

// scalastyle:off file.size.limit
class EditCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {
  "onPageLoad" must {
    "return OK and the correct view for a GET" in new Setup {

      val application: Application = applicationBuilder(Some(userAnswers))
        .overrides(
          inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
          inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector),
          inject.bind[CheckYourAnswersValidationService].toInstance(mockValidator),
          inject.bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers)),
          inject.bind[AuthoritiesRepository].toInstance(mockAuthoritiesRepo)
        )
        .configure(Map("features.edit-journey" -> true))
        .build()

      when(mockDataStoreConnector.retrieveCompanyInformationThirdParty(any())(any()))
        .thenReturn(Future.successful(Some("This business has not consented to their name being shared.")))

      when(mockAuthoritiesRepo.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))
      when(mockConnector.retrieveAccountAuthorities(any)(any)).thenReturn(
        Future.successful(Seq(accWithAuthorities1))
      )

      when(mockAuthCacheService.retrieveAuthorities(any, any)(any)).thenReturn(
        Future.successful(authoritiesWithId)
      )

      when(mockAuthCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(
          Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthorityForGB)))
        )

      running(application) {
        val request   = fakeRequest(GET, authorisedUserRoute)
        val result    = route(application, request).value
        val view      = application.injector.instanceOf[EditCheckYourAnswersView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(helper(userAnswers, application, standingAuthorityForGB), "a", "b")(
            request,
            messages(application),
            appConfig
          ).toString
      }
    }

    "redirect to Session Expired for a POST if no existing data is found" in new Setup {
      val application: Application =
        applicationBuilder(userAnswers = None).configure(Map("features.edit-journey" -> true)).build()

      running(application) {
        val request =
          fakeRequest(POST, authorisedUserRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url
      }
    }

    "Redirect to TechnicalDifficulties page when NoAccount is return for " +
      "AuthoritiesCacheService.getAccountAndAuthority call" in new Setup {

        val application: Application = applicationBuilder(Some(userAnswers))
          .overrides(
            inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
            inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector),
            inject.bind[CheckYourAnswersValidationService].toInstance(mockValidator),
            inject.bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers)),
            inject.bind[AuthoritiesRepository].toInstance(mockAuthoritiesRepo)
          )
          .configure(Map("features.edit-journey" -> true))
          .build()

        when(mockDataStoreConnector.getCompanyName(any()))
          .thenReturn(Future.successful(Some("This business has not consented to their name being shared.")))

        when(mockAuthoritiesRepo.get(any()))
          .thenReturn(Future.successful(Some(authoritiesWithId.copy(authorities = Map()))))
        when(mockConnector.retrieveAccountAuthorities(any)(any)).thenReturn(
          Future.successful(Seq(accWithAuthorities1))
        )
        when(mockAuthCacheService.retrieveAuthorities(any, any)(any)).thenReturn(
          Future.successful(authoritiesWithId.copy(authorities = Map.empty))
        )
        when(mockAuthCacheService.getAccountAndAuthority(any(), any(), any())(any()))
          .thenReturn(Future.successful(Left(NoAccount)))

        running(application) {
          val request = fakeRequest(GET, authorisedUserRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.TechnicalDifficulties.onPageLoad.url)
        }
      }

    "Redirect to TechnicalDifficulties when NoAuthority is return for " +
      "AuthoritiesCacheService.getAccountAndAuthority call" in new Setup {

        val application: Application = applicationBuilder(Some(userAnswers))
          .overrides(
            inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
            inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector),
            inject.bind[CheckYourAnswersValidationService].toInstance(mockValidator),
            inject.bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers)),
            inject.bind[AuthoritiesRepository].toInstance(mockAuthoritiesRepo)
          )
          .configure(Map("features.edit-journey" -> true))
          .build()

        when(mockDataStoreConnector.getCompanyName(any()))
          .thenReturn(Future.successful(Some("This business has not consented to their name being shared.")))

        val optAccountWithAuthoritiesWithId: Option[AccountWithAuthoritiesWithId] =
          authoritiesWithId.authorities.get("a")
        val updatedAccountWithAuthoritiesWithId: AccountWithAuthoritiesWithId     =
          optAccountWithAuthoritiesWithId.get.copy(authorities = Map())

        when(mockAuthoritiesRepo.get(any())).thenReturn(
          Future.successful(Some(authoritiesWithId.copy(authorities = Map("a" -> updatedAccountWithAuthoritiesWithId))))
        )
        when(mockConnector.retrieveAccountAuthorities(any)(any)).thenReturn(
          Future.successful(Seq(accWithAuthorities1))
        )
        when(mockAuthCacheService.retrieveAuthorities(any, any)(any)).thenReturn(
          Future.successful(authoritiesWithId.copy(authorities = Map("a" -> updatedAccountWithAuthoritiesWithId)))
        )
        when(mockAuthCacheService.getAccountAndAuthority(any(), any(), any())(any()))
          .thenReturn(Future.successful(Left(NoAuthority)))

        running(application) {
          val request = fakeRequest(GET, authorisedUserRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.TechnicalDifficulties.onPageLoad.url)
        }
      }
  }

  "onSubmit" must {
    "Redirect to next page when valid data is submitted " in new Setup {

      val application: Application = applicationBuilder(Some(userAnswers))
        .overrides(
          inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
          inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector),
          inject.bind[CheckYourAnswersValidationService].toInstance(mockValidator),
          inject.bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers)),
          inject.bind[AuthoritiesRepository].toInstance(mockAuthoritiesRepo),
          inject.bind[EditAuthorityValidationService].toInstance(mockEditAuthorityValidationService)
        )
        .configure(Map("features.edit-journey" -> true))
        .build()

      when(mockDataStoreConnector.getCompanyName(any()))
        .thenReturn(Future.successful(Some("This business has not consented to their name being shared.")))

      when(mockAuthoritiesRepo.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))
      when(mockConnector.retrieveAccountAuthorities(any)(any)).thenReturn(
        Future.successful(Seq(accWithAuthorities1))
      )
      when(mockAuthCacheService.retrieveAuthorities(any, any)(any)).thenReturn(
        Future.successful(authoritiesWithId)
      )
      when(mockAuthCacheService.getAccountAndAuthority(any(), any(), any())(any()))
        .thenReturn(
          Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthorityForGB)))
        )

      when(mockDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some("XI123456789012")))

      override val accounts: Accounts = Accounts(
        Some(AccountWithAuthorities(CdsCashAccount, "123456", Some(AccountStatusOpen), Seq.empty)),
        Seq.empty,
        None
      )
      when(mockEditAuthorityValidationService.validate(any, any, any, any, any, any)).thenReturn(
        Right(
          AddAuthorityRequest(
            accounts,
            standingAuthorityForGB,
            AuthorisedUser("someName", "someRole"),
            editRequest = true,
            ownerEori
          )
        )
      )

      when(mockConnector.grantAccountAuthorities(any)(any)).thenReturn(Future.successful(true))

      running(application) {
        val request = fakeRequest(POST, onSubmitRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "Redirect to next page for valid data when user has selected cash,guarantee and DD accounts is submitted " +
      "and authorised EORI is XI EORI" in new Setup {

        val application: Application = applicationBuilder(Some(userAnswers), gbEori)
          .overrides(
            inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
            inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector),
            inject.bind[CheckYourAnswersValidationService].toInstance(mockValidator),
            inject.bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers)),
            inject.bind[AuthoritiesRepository].toInstance(mockAuthoritiesRepo),
            inject.bind[EditAuthorityValidationService].toInstance(mockEditAuthorityValidationService),
            inject.bind[AuthoritiesCacheService].toInstance(mockAuthCacheService)
          )
          .configure(Map("features.edit-journey" -> true))
          .build()

        when(mockDataStoreConnector.getCompanyName(any()))
          .thenReturn(Future.successful(Some("This business has not consented to their name being shared.")))

        when(mockAuthoritiesRepo.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))
        when(mockConnector.retrieveAccountAuthorities(any)(any)).thenReturn(
          Future.successful(Seq(accWithAuthorities1))
        )
        when(mockAuthCacheService.retrieveAuthorities(any, any)(any)).thenReturn(
          Future.successful(authoritiesWithId)
        )
        when(mockAuthCacheService.getAccountAndAuthority(any(), any(), any())(any()))
          .thenReturn(
            Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthorityForXI)))
          )

        when(mockDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some("XI123456789012")))

        val validatedAddAuthorityRequest: AddAuthorityRequest = AddAuthorityRequest(
          accountTypes,
          standingAuthorityForXI,
          AuthorisedUser("someName", "someRole"),
          editRequest = true,
          ownerEori = xiEori
        )

        when(mockEditAuthorityValidationService.validate(any, any, any, any, any, any))
          .thenReturn(Right(validatedAddAuthorityRequest))

        val xiAddAuthorityRequest: AddAuthorityRequest = validatedAddAuthorityRequest.copy(
          accounts = accountTypes.copy(
            cash = None,
            guarantee = None,
            dutyDeferments = Seq("DutyDeferment")
          ),
          ownerEori = xiEori
        )

        val gbAddAuthorityRequest: AddAuthorityRequest = validatedAddAuthorityRequest.copy(
          accounts = accountTypes.copy(
            cash = Some("CDSCash"),
            guarantee = Some("GeneralGuarantee"),
            dutyDeferments = Seq.empty
          ),
          ownerEori = gbEori
        )

        when(mockConnector.grantAccountAuthorities(eqMatcher(xiAddAuthorityRequest))(any))
          .thenReturn(Future.successful(true))
        when(mockConnector.grantAccountAuthorities(eqMatcher(gbAddAuthorityRequest))(any))
          .thenReturn(Future.successful(true))

        running(application) {
          val request = fakeRequest(POST, onSubmitRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          verify(mockConnector, Mockito.times(2)).grantAccountAuthorities(any)(any)
        }
      }

    "Redirect to next page for valid data when user has selected cash, guarantee and DD accounts is submitted " +
      "and authorised EORI is XI EORI but grant authority calls fail" in new Setup {

        val application: Application = applicationBuilder(Some(userAnswers), gbEori)
          .overrides(
            inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
            inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector),
            inject.bind[CheckYourAnswersValidationService].toInstance(mockValidator),
            inject.bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers)),
            inject.bind[AuthoritiesRepository].toInstance(mockAuthoritiesRepo),
            inject.bind[EditAuthorityValidationService].toInstance(mockEditAuthorityValidationService),
            inject.bind[AuthoritiesCacheService].toInstance(mockAuthCacheService)
          )
          .configure(Map("features.edit-journey" -> true))
          .build()

        when(mockDataStoreConnector.getCompanyName(any()))
          .thenReturn(Future.successful(Some("This business has not consented to their name being shared.")))

        when(mockAuthoritiesRepo.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))
        when(mockConnector.retrieveAccountAuthorities(any)(any)).thenReturn(
          Future.successful(Seq(accWithAuthorities1))
        )
        when(mockAuthCacheService.retrieveAuthorities(any, any)(any)).thenReturn(
          Future.successful(authoritiesWithId)
        )
        when(mockAuthCacheService.getAccountAndAuthority(any(), any(), any())(any()))
          .thenReturn(
            Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthorityForXI)))
          )

        when(mockDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some("XI123456789012")))

        val validatedAddAuthorityRequest: AddAuthorityRequest = AddAuthorityRequest(
          accountTypes,
          standingAuthorityForXI,
          AuthorisedUser("someName", "someRole"),
          editRequest = true,
          ownerEori = xiEori
        )

        when(mockEditAuthorityValidationService.validate(any, any, any, any, any, any))
          .thenReturn(Right(validatedAddAuthorityRequest))

        val xiAddAuthorityRequest: AddAuthorityRequest = validatedAddAuthorityRequest.copy(
          accounts = accountTypes.copy(
            cash = None,
            guarantee = None,
            dutyDeferments = Seq("DutyDeferment")
          ),
          ownerEori = xiEori
        )

        val gbAddAuthorityRequest: AddAuthorityRequest = validatedAddAuthorityRequest.copy(
          accounts = accountTypes.copy(
            cash = Some("CDSCash"),
            guarantee = Some("GeneralGuarantee"),
            dutyDeferments = Seq.empty
          ),
          ownerEori = gbEori
        )

        when(mockConnector.grantAccountAuthorities(eqMatcher(xiAddAuthorityRequest))(any))
          .thenReturn(Future.successful(false))
        when(mockConnector.grantAccountAuthorities(eqMatcher(gbAddAuthorityRequest))(any))
          .thenReturn(Future.successful(false))

        running(application) {
          val request = fakeRequest(POST, onSubmitRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.TechnicalDifficulties.onPageLoad.url)
          verify(mockConnector, Mockito.times(2)).grantAccountAuthorities(any)(any)
        }
      }

    "Redirect to next page for valid data when user has selected cash,guarantee and DD accounts is submitted " +
      "and authorised EORI is XI EORI but one of grant authority call fails" in new Setup {

        val application: Application = applicationBuilder(Some(userAnswers), gbEori)
          .overrides(
            inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
            inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector),
            inject.bind[CheckYourAnswersValidationService].toInstance(mockValidator),
            inject.bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers)),
            inject.bind[AuthoritiesRepository].toInstance(mockAuthoritiesRepo),
            inject.bind[EditAuthorityValidationService].toInstance(mockEditAuthorityValidationService),
            inject.bind[AuthoritiesCacheService].toInstance(mockAuthCacheService)
          )
          .configure(Map("features.edit-journey" -> true))
          .build()

        when(mockDataStoreConnector.getCompanyName(any()))
          .thenReturn(Future.successful(Some("This business has not consented to their name being shared.")))

        when(mockAuthoritiesRepo.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))
        when(mockConnector.retrieveAccountAuthorities(any)(any)).thenReturn(
          Future.successful(Seq(accWithAuthorities1))
        )
        when(mockAuthCacheService.retrieveAuthorities(any, any)(any)).thenReturn(
          Future.successful(authoritiesWithId)
        )
        when(mockAuthCacheService.getAccountAndAuthority(any(), any(), any())(any()))
          .thenReturn(
            Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthorityForXI)))
          )

        when(mockDataStoreConnector.getXiEori(any)(any))
          .thenReturn(Future.successful(Some("XI123456789012")))

        val validatedAddAuthorityRequest: AddAuthorityRequest = AddAuthorityRequest(
          accountTypes,
          standingAuthorityForXI,
          AuthorisedUser("someName", "someRole"),
          editRequest = true,
          ownerEori = xiEori
        )

        when(mockEditAuthorityValidationService.validate(any, any, any, any, any, any))
          .thenReturn(Right(validatedAddAuthorityRequest))

        val xiAddAuthorityRequest: AddAuthorityRequest = validatedAddAuthorityRequest.copy(
          accounts = accountTypes.copy(
            cash = None,
            guarantee = None,
            dutyDeferments = Seq("DutyDeferment")
          ),
          ownerEori = xiEori
        )

        val gbAddAuthorityRequest: AddAuthorityRequest = validatedAddAuthorityRequest.copy(
          accounts = accountTypes.copy(
            cash = Some("CDSCash"),
            guarantee = Some("GeneralGuarantee"),
            dutyDeferments = Seq.empty
          ),
          ownerEori = gbEori
        )

        when(mockConnector.grantAccountAuthorities(eqMatcher(xiAddAuthorityRequest))(any))
          .thenReturn(Future.successful(true))
        when(mockConnector.grantAccountAuthorities(eqMatcher(gbAddAuthorityRequest))(any))
          .thenReturn(Future.successful(false))

        running(application) {
          val request = fakeRequest(POST, onSubmitRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.TechnicalDifficulties.onPageLoad.url)
          verify(mockConnector, Mockito.times(2)).grantAccountAuthorities(any)(any)
        }
      }

    "Redirect to next page for valid data when user has selected cash,guarantee and DD accounts is submitted " +
      "and authorised EORI is GB EORI" in new Setup {

        val application: Application = applicationBuilder(Some(userAnswers), gbEori)
          .overrides(
            inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
            inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector),
            inject.bind[CheckYourAnswersValidationService].toInstance(mockValidator),
            inject.bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers)),
            inject.bind[AuthoritiesRepository].toInstance(mockAuthoritiesRepo),
            inject.bind[EditAuthorityValidationService].toInstance(mockEditAuthorityValidationService),
            inject.bind[AuthoritiesCacheService].toInstance(mockAuthCacheService)
          )
          .configure(Map("features.edit-journey" -> true))
          .build()

        when(mockDataStoreConnector.getCompanyName(any()))
          .thenReturn(Future.successful(Some("This business has not consented to their name being shared.")))

        when(mockAuthoritiesRepo.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))
        when(mockConnector.retrieveAccountAuthorities(any)(any)).thenReturn(
          Future.successful(Seq(accWithAuthorities1))
        )
        when(mockAuthCacheService.retrieveAuthorities(any, any)(any)).thenReturn(
          Future.successful(authoritiesWithId)
        )
        when(mockAuthCacheService.getAccountAndAuthority(any(), any(), any())(any()))
          .thenReturn(
            Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthorityForGB)))
          )

        when(mockDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some("XI123456789012")))

        val validatedAddAuthorityRequest: AddAuthorityRequest = AddAuthorityRequest(
          accountTypes,
          standingAuthorityForGB,
          AuthorisedUser("someName", "someRole"),
          editRequest = true,
          ownerEori = xiEori
        )

        when(mockEditAuthorityValidationService.validate(any, any, any, any, any, any))
          .thenReturn(Right(validatedAddAuthorityRequest))

        val gbAddAuthorityRequest1: AddAuthorityRequest = validatedAddAuthorityRequest.copy(
          accounts = accountTypes.copy(
            cash = None,
            guarantee = None,
            dutyDeferments = Seq("DutyDeferment")
          ),
          ownerEori = xiEori
        )

        val gbAddAuthorityRequest2: AddAuthorityRequest = validatedAddAuthorityRequest.copy(
          accounts = accountTypes.copy(
            cash = Some("CDSCash"),
            guarantee = Some("GeneralGuarantee"),
            dutyDeferments = Seq.empty
          ),
          ownerEori = gbEori
        )

        when(mockConnector.grantAccountAuthorities(eqMatcher(gbAddAuthorityRequest1))(any))
          .thenReturn(Future.successful(true))
        when(mockConnector.grantAccountAuthorities(eqMatcher(gbAddAuthorityRequest2))(any))
          .thenReturn(Future.successful(true))

        running(application) {
          val request = fakeRequest(POST, onSubmitRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          verify(mockConnector, Mockito.times(1)).grantAccountAuthorities(any)(any)
        }
      }

    "Redirect to next page for valid data when user has selected cash, guarantee and DD accounts is submitted " +
      "and authorised EORI is EU EORI" in new Setup {

        val application: Application = applicationBuilder(Some(userAnswers), euEori)
          .overrides(
            inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
            inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector),
            inject.bind[CheckYourAnswersValidationService].toInstance(mockValidator),
            inject.bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers)),
            inject.bind[AuthoritiesRepository].toInstance(mockAuthoritiesRepo),
            inject.bind[EditAuthorityValidationService].toInstance(mockEditAuthorityValidationService),
            inject.bind[AuthoritiesCacheService].toInstance(mockAuthCacheService)
          )
          .configure(Map("features.edit-journey" -> true))
          .build()

        when(mockDataStoreConnector.getCompanyName(any()))
          .thenReturn(Future.successful(Some("This business has not consented to their name being shared.")))

        when(mockAuthoritiesRepo.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))
        when(mockConnector.retrieveAccountAuthorities(any)(any)).thenReturn(
          Future.successful(Seq(accWithAuthorities1))
        )
        when(mockAuthCacheService.retrieveAuthorities(any, any)(any)).thenReturn(
          Future.successful(authoritiesWithId)
        )
        when(mockAuthCacheService.getAccountAndAuthority(any(), any(), any())(any()))
          .thenReturn(
            Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthorityForGB)))
          )

        when(mockDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(None))

        val validatedAddAuthorityRequest: AddAuthorityRequest = AddAuthorityRequest(
          accountTypes,
          standingAuthorityForEU,
          AuthorisedUser("someName", "someRole"),
          editRequest = true,
          ownerEori = xiEori
        )

        when(mockEditAuthorityValidationService.validate(any, any, any, any, any, any))
          .thenReturn(Right(validatedAddAuthorityRequest))

        val euAddAuthorityRequest1: AddAuthorityRequest = validatedAddAuthorityRequest.copy(
          accounts = accountTypes.copy(
            cash = None,
            guarantee = None,
            dutyDeferments = Seq("DutyDeferment")
          ),
          ownerEori = xiEori
        )

        val euAddAuthorityRequest2: AddAuthorityRequest = validatedAddAuthorityRequest.copy(
          accounts = accountTypes.copy(
            cash = Some("CDSCash"),
            guarantee = Some("GeneralGuarantee"),
            dutyDeferments = Seq.empty
          ),
          ownerEori = gbEori
        )

        when(mockConnector.grantAccountAuthorities(eqMatcher(euAddAuthorityRequest1))(any))
          .thenReturn(Future.successful(true))
        when(mockConnector.grantAccountAuthorities(eqMatcher(euAddAuthorityRequest2))(any))
          .thenReturn(Future.successful(true))

        running(application) {
          val request = fakeRequest(POST, onSubmitRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          verify(mockConnector, Mockito.times(1)).grantAccountAuthorities(any)(any)
        }
      }

    "Redirect to next page when valid data is submitted for AccountAuthority with XI Eori " +
      "as authorised EORI " in new Setup {

        val application: Application = applicationBuilder(Some(userAnswers))
          .overrides(
            inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
            inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector),
            inject.bind[CheckYourAnswersValidationService].toInstance(mockValidator),
            inject.bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers)),
            inject.bind[AuthoritiesRepository].toInstance(mockAuthoritiesRepo),
            inject.bind[EditAuthorityValidationService].toInstance(mockEditAuthorityValidationService)
          )
          .configure(Map("features.edit-journey" -> true))
          .build()

        override val accounts: Accounts = Accounts(
          Some(AccountWithAuthorities(CdsCashAccount, "123456", Some(AccountStatusOpen), Seq.empty)),
          Seq.empty,
          None
        )

        when(mockDataStoreConnector.getCompanyName(any()))
          .thenReturn(Future.successful(Some("This business has not consented to their name being shared.")))

        when(mockAuthoritiesRepo.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))
        when(mockConnector.retrieveAccountAuthorities(any)(any)).thenReturn(
          Future.successful(Seq(accWithAuthorities1))
        )
        when(mockAuthCacheService.retrieveAuthorities(any, any)(any)).thenReturn(
          Future.successful(authoritiesWithId)
        )
        when(mockAuthCacheService.getAccountAndAuthority(any(), any(), any())(any()))
          .thenReturn(
            Future.successful(
              Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthorityForXI))
            )
          )

        when(mockDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some("XI123456789012")))

        when(mockEditAuthorityValidationService.validate(any, any, any, any, any, any)).thenReturn(
          Right(
            AddAuthorityRequest(
              accounts,
              standingAuthorityForGB,
              AuthorisedUser("someName", "someRole"),
              editRequest = true,
              ownerEori
            )
          )
        )

        when(mockConnector.grantAccountAuthorities(any)(any)).thenReturn(Future.successful(true))

        running(application) {
          val request = fakeRequest(POST, onSubmitRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
        }
      }

    "Redirect to TechnicalDifficulties when EditAuthorityValidationService.validate " +
      "returns positive response but account authorities are not granted" in new Setup {

        val application: Application = applicationBuilder(Some(userAnswers))
          .overrides(
            inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
            inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector),
            inject.bind[CheckYourAnswersValidationService].toInstance(mockValidator),
            inject.bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers)),
            inject.bind[AuthoritiesRepository].toInstance(mockAuthoritiesRepo),
            inject.bind[EditAuthorityValidationService].toInstance(mockEditAuthorityValidationService)
          )
          .configure(Map("features.edit-journey" -> true))
          .build()

        when(mockDataStoreConnector.getCompanyName(any()))
          .thenReturn(Future.successful(Some("This business has not consented to their name being shared.")))

        when(mockAuthoritiesRepo.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))
        when(mockConnector.retrieveAccountAuthorities(any)(any)).thenReturn(
          Future.successful(Seq(accWithAuthorities1))
        )
        when(mockAuthCacheService.retrieveAuthorities(any, any)(any)).thenReturn(
          Future.successful(authoritiesWithId)
        )
        when(mockAuthCacheService.getAccountAndAuthority(any(), any(), any())(any()))
          .thenReturn(
            Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthorityForGB)))
          )

        when(mockDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some("XI123456789012")))

        override val accounts: Accounts = Accounts(
          Some(AccountWithAuthorities(CdsCashAccount, "123456", Some(AccountStatusOpen), Seq.empty)),
          Seq.empty,
          None
        )
        when(mockEditAuthorityValidationService.validate(any, any, any, any, any, any)).thenReturn(
          Right(
            AddAuthorityRequest(
              accounts,
              standingAuthorityForGB,
              AuthorisedUser("someName", "someRole"),
              editRequest = true,
              ownerEori
            )
          )
        )

        when(mockConnector.grantAccountAuthorities(any)(any)).thenReturn(Future.successful(false))

        running(application) {
          val request = fakeRequest(POST, onSubmitRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.TechnicalDifficulties.onPageLoad.url)
        }
      }

    "Redirect to TechnicalDifficulties when EditAuthorityValidationService.validate" +
      "return error response" in new Setup {

        val application: Application = applicationBuilder(Some(userAnswers))
          .overrides(
            inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
            inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector),
            inject.bind[CheckYourAnswersValidationService].toInstance(mockValidator),
            inject.bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers)),
            inject.bind[AuthoritiesRepository].toInstance(mockAuthoritiesRepo),
            inject.bind[EditAuthorityValidationService].toInstance(mockEditAuthorityValidationService)
          )
          .configure(Map("features.edit-journey" -> true))
          .build()

        when(mockDataStoreConnector.getCompanyName(any()))
          .thenReturn(Future.successful(Some("This business has not consented to their name being shared.")))

        when(mockAuthoritiesRepo.get(any())).thenReturn(Future.successful(Some(authoritiesWithId)))
        when(mockConnector.retrieveAccountAuthorities(any)(any)).thenReturn(
          Future.successful(Seq(accWithAuthorities1))
        )
        when(mockAuthCacheService.retrieveAuthorities(any, any)(any)).thenReturn(
          Future.successful(authoritiesWithId)
        )
        when(mockAuthCacheService.getAccountAndAuthority(any(), any(), any())(any()))
          .thenReturn(
            Future.successful(Right(AccountAndAuthority(accountsWithAuthoritiesWithId, standingAuthorityForGB)))
          )

        when(mockDataStoreConnector.getXiEori(any)(any)).thenReturn(Future.successful(Some("XI123456789012")))
        when(mockEditAuthorityValidationService.validate(any, any, any, any, any, any))
          .thenReturn(Left(UnknownAccountType))

        running(application) {
          val request = fakeRequest(POST, onSubmitRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.TechnicalDifficulties.onPageLoad.url)
        }
      }

    "Redirect to TechnicalDifficulties when when NoAuthority is return for " +
      "AuthoritiesCacheService.getAccountAndAuthority call" in new Setup {

        val application: Application = applicationBuilder(Some(userAnswers))
          .overrides(
            inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
            inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector),
            inject.bind[CheckYourAnswersValidationService].toInstance(mockValidator),
            inject.bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers)),
            inject.bind[AuthoritiesRepository].toInstance(mockAuthoritiesRepo)
          )
          .configure(Map("features.edit-journey" -> true))
          .build()

        when(mockDataStoreConnector.getCompanyName(any()))
          .thenReturn(Future.successful(Some("This business has not consented to their name being shared.")))

        val optAccountWithAuthoritiesWithId: Option[AccountWithAuthoritiesWithId] =
          authoritiesWithId.authorities.get("a")
        val updatedAccountWithAuthoritiesWithId: AccountWithAuthoritiesWithId     =
          optAccountWithAuthoritiesWithId.get.copy(authorities = Map())

        when(mockAuthoritiesRepo.get(any())).thenReturn(
          Future.successful(Some(authoritiesWithId.copy(authorities = Map("a" -> updatedAccountWithAuthoritiesWithId))))
        )
        when(mockConnector.retrieveAccountAuthorities(any)(any)).thenReturn(
          Future.successful(Seq(accWithAuthorities1))
        )
        when(mockAuthCacheService.retrieveAuthorities(any, any)(any)).thenReturn(
          Future.successful(authoritiesWithId.copy(authorities = Map("a" -> updatedAccountWithAuthoritiesWithId)))
        )
        when(mockAuthCacheService.getAccountAndAuthority(any(), any(), any())(any()))
          .thenReturn(Future.successful(Left(NoAuthority)))

        running(application) {
          val request = fakeRequest(POST, onSubmitRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.TechnicalDifficulties.onPageLoad.url)
        }
      }

    "Redirect to TechnicalDifficulties page when NoAccount is return for " +
      "AuthoritiesCacheService.getAccountAndAuthority call" in new Setup {

        val application: Application = applicationBuilder(Some(userAnswers))
          .overrides(
            inject.bind[CustomsFinancialsConnector].toInstance(mockConnector),
            inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector),
            inject.bind[CheckYourAnswersValidationService].toInstance(mockValidator),
            inject.bind[VerifyAccountNumbersAction].toInstance(new FakeVerifyAccountNumbersAction(userAnswers)),
            inject.bind[AuthoritiesRepository].toInstance(mockAuthoritiesRepo)
          )
          .configure(Map("features.edit-journey" -> true))
          .build()

        when(mockDataStoreConnector.getCompanyName(any()))
          .thenReturn(Future.successful(Some("This business has not consented to their name being shared.")))

        when(mockAuthoritiesRepo.get(any()))
          .thenReturn(Future.successful(Some(authoritiesWithId.copy(authorities = Map()))))
        when(mockConnector.retrieveAccountAuthorities(any)(any)).thenReturn(
          Future.successful(Seq(accWithAuthorities1))
        )
        when(mockAuthCacheService.retrieveAuthorities(any, any)(any)).thenReturn(
          Future.successful(authoritiesWithId.copy(authorities = Map.empty))
        )
        when(mockAuthCacheService.getAccountAndAuthority(any(), any(), any())(any()))
          .thenReturn(Future.successful(Left(NoAccount)))

        running(application) {
          val request = fakeRequest(POST, onSubmitRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.TechnicalDifficulties.onPageLoad.url)
        }
      }
  }

  trait Setup {
    private val oneDay  = 1
    private val twoDays = 2

    private val accountId = "a"
    private val authId    = "b"

    val gbEori    = "GB123456789012"
    val euEori    = "DE123456789012"
    val xiEori    = "XI123456789012"
    val ownerEori = "someEori"

    def onwardRoute: Call = Call("GET", "/foo")

    lazy val authorisedUserRoute: String =
      controllers.edit.routes.EditCheckYourAnswersController.onPageLoad(accountId, authId).url

    lazy val onSubmitRoute: String =
      controllers.edit.routes.EditCheckYourAnswersController.onSubmit(accountId, authId).url

    val mockConnector: CustomsFinancialsConnector                          = mock[CustomsFinancialsConnector]
    val mockDataStoreConnector: CustomsDataStoreConnector                  = mock[CustomsDataStoreConnector]
    val mockAuthCacheService: AuthoritiesCacheService                      = mock[AuthoritiesCacheService]
    val mockEditAuthorityValidationService: EditAuthorityValidationService = mock[EditAuthorityValidationService]
    val mockValidator: CheckYourAnswersValidationService                   = mock[CheckYourAnswersValidationService]
    val mockAuthoritiesRepo: AuthoritiesRepository                         = mock[AuthoritiesRepository]
    val mockDateTimeService: DateTimeService                               = mock[DateTimeService]

    val userAnswers: UserAnswers = emptyUserAnswers
      .set(EditAuthorityStartDatePage(accountId, authId), LocalDate.now())
      .get
      .set(EditAuthorityStartPage(accountId, authId), Today)
      .get
      .set(EditAuthorityEndPage(accountId, authId), Indefinite)
      .get
      .set(EditShowBalancePage(accountId, authId), Yes)
      .get
      .set(EditAuthorisedUserPage(accountId, authId), AuthorisedUser("test", "test"))
      .get

    def populatedUserAnswers(userAnswers: UserAnswers): UserAnswers =
      userAnswers
        .set(EditShowBalancePage(accountId, authId), ShowBalance.Yes)(ShowBalance.writes)
        .success
        .value
        .set(EditAuthorityStartPage(accountId, authId), AuthorityStart.Today)(AuthorityStart.writes)
        .success
        .value
        .set(EditAuthorityEndPage(accountId, authId), AuthorityEnd.Indefinite)(AuthorityEnd.writes)
        .success
        .value

    val standingAuthorityForGB: StandingAuthority =
      domain.StandingAuthority(gbEori, LocalDate.now(), None, viewBalance = true)

    val standingAuthorityForXI: StandingAuthority =
      domain.StandingAuthority(xiEori, LocalDate.now(), None, viewBalance = true)

    val standingAuthorityForEU: StandingAuthority =
      domain.StandingAuthority(euEori, LocalDate.now(), None, viewBalance = true)

    val accountsWithAuthoritiesWithId: AccountWithAuthoritiesWithId =
      AccountWithAuthoritiesWithId(
        CdsCashAccount,
        "123456",
        Some(AccountStatusOpen),
        Map("b" -> standingAuthorityForGB)
      )
    val authoritiesWithId: AuthoritiesWithId                        = AuthoritiesWithId(
      Map(
        "a" -> accountsWithAuthoritiesWithId
      )
    )

    val standingAuthorityPast: StandingAuthority =
      StandingAuthority(gbEori, LocalDate.now().minusDays(twoDays), None, viewBalance = true)

    val accountsWithAuthoritiesWithIdPast: AccountWithAuthoritiesWithId =
      AccountWithAuthoritiesWithId(CdsCashAccount, "123456", Some(AccountStatusOpen), Map("b" -> standingAuthorityPast))
    val authoritiesWithIdPast: AuthoritiesWithId                        = AuthoritiesWithId(
      Map(
        "a" -> accountsWithAuthoritiesWithIdPast
      )
    )

    val standingAuthority1: StandingAuthority =
      StandingAuthority(gbEori, LocalDate.now(), Option(LocalDate.now().plusDays(oneDay)), viewBalance = true)

    val standingAuthority2: StandingAuthority =
      StandingAuthority(gbEori, LocalDate.now(), Option(LocalDate.now().plusDays(oneDay)), viewBalance = true)

    val accWithAuthorities1: AccountWithAuthorities = AccountWithAuthorities(
      CdsCashAccount,
      "123",
      Option(AccountStatusOpen),
      Seq(standingAuthority1, standingAuthority2)
    )

    val accounts: Accounts = Accounts(
      Some(AccountWithAuthorities(CdsCashAccount, "123456", Some(AccountStatusOpen), Seq.empty)),
      Seq("123456"),
      Some("123456")
    )

    val accountTypes: Accounts = Accounts(
      cash = Some("CDSCash"),
      dutyDeferments = Seq("DutyDeferment"),
      guarantee = Some("GeneralGuarantee")
    )

    val authorisedUser: AuthorisedUser = AuthorisedUser("someName", "someRole")

    def helper(
      userAnswers: UserAnswers,
      application: Application,
      authority: StandingAuthority = standingAuthorityForGB
    ): CheckYourAnswersEditHelper =
      new CheckYourAnswersEditHelper(
        populatedUserAnswers(userAnswers),
        accountId,
        authId,
        mockDateTimeService,
        authority,
        accountsWithAuthoritiesWithId,
        None
      )(messages(application))

    when(mockDateTimeService.localTime()).thenReturn(LocalDateTime.now())
    when(mockDateTimeService.localDate()).thenReturn(LocalDate.now())
    when(mockConnector.grantAccountAuthorities(any())(any())).thenReturn(Future.successful(true))
  }
}
